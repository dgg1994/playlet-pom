package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.OnePayWithdrawPayoutRequest;
import com.playlet.internal.config.OnePayProperties;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.welfare.UserCoinLedgerDao;
import com.playlet.internal.dao.welfare.UserWithdrawOrderDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.welfare.UserCoinLedgerEntity;
import com.playlet.internal.entity.welfare.UserWithdrawOrderEntity;
import com.playlet.internal.enums.CoinBizTypeEnums;
import com.playlet.internal.enums.WithdrawOrderStatusEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.WithdrawPayoutService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.TransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * OnePay 打款：提交后只冻结；确认到账再解冻并扣减。
 */
@Slf4j
@Service
public class WithdrawPayoutServiceImpl implements WithdrawPayoutService {

	@Value("${withdraw.mock-success:true}")
	private boolean mockSuccess;

	@Autowired
	private UserWithdrawOrderDao userWithdrawOrderDao;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private UserCoinLedgerDao userCoinLedgerDao;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private OnePayProperties onePayProperties;

	@Lazy
	@Autowired
	private WithdrawPayoutServiceImpl self;

	@Override
	@Async("asyncExecutor")
	public void payoutAsync(Long orderId) {
		if (orderId == null) {
			return;
		}
		try {
			if (!self.markPaying(orderId)) {
				return;
			}
			UserWithdrawOrderEntity order = userWithdrawOrderDao.selectById(orderId);
			if (order == null) {
				return;
			}
			if (StringUtils.isEmpty(onePayProperties.getWithdrawUrl())) {
				// 未配置打款地址：走 mock，便于联调
				if (mockSuccess) {
					self.confirmSuccess(order.getOrderNo(), "MOCK_" + order.getOrderNo(), null);
				} else {
					self.failAndUnfreeze(order.getOrderNo(), "mock payout failed");
				}
				return;
			}
			if (!callOnePay(order)) {
				self.failAndUnfreeze(order.getOrderNo(), "onepay reject");
			}
			// HTTP 200 仅表示受理，等回调再解冻扣减
		} catch (Exception e) {
			log.error("withdraw payout failed orderId={}", orderId, e);
			UserWithdrawOrderEntity order = userWithdrawOrderDao.selectById(orderId);
			if (order != null && !StringUtils.isEmpty(order.getOrderNo())) {
				try {
					self.failAndUnfreeze(order.getOrderNo(), "onepay error");
				} catch (Exception ex) {
					log.error("withdraw payout compensate failed orderId={}", orderId, ex);
				}
			}
		}
	}

	@Override
	public void handleCallback(String orderNo, boolean success, String thirdOrderNo, String failReason) {
		if (success) {
			self.confirmSuccess(orderNo, thirdOrderNo, failReason);
			return;
		}
		self.failAndUnfreeze(orderNo, failReason);
	}

	/** PENDING → PAYING，保证只打款一次 */
	@Transactional(rollbackFor = Exception.class)
	public boolean markPaying(Long orderId) {
		UserWithdrawOrderEntity order = userWithdrawOrderDao.selectById(orderId);
		if (order == null) {
			return false;
		}
		if (!Integer.valueOf(WithdrawOrderStatusEnums.PENDING.getCode()).equals(order.getStatus())) {
			return false;
		}
		int moved = userWithdrawOrderDao.casStatus(orderId,
				WithdrawOrderStatusEnums.PENDING.getCode(),
				WithdrawOrderStatusEnums.PAYING.getCode());
		return moved > 0;
	}

	/** OnePay 确认到账：解冻并扣减金币 */
	@Transactional(rollbackFor = Exception.class)
	public void confirmSuccess(String orderNo, String thirdOrderNo, String failReason) {
		UserWithdrawOrderEntity order = loadPayingOrPending(orderNo);
		if (order == null) {
			return;
		}
		int fromStatus = order.getStatus();
		int finished = userWithdrawOrderDao.casFinish(order.getId(), fromStatus,
				WithdrawOrderStatusEnums.SUCCESS.getCode(),
				thirdOrderNo, failReason);
		if (finished <= 0) {
			return;
		}
		int amt = nvlPoints(order.getPointsAmt());
		try {
			writeWithdrawLedger(order.getUid(), amt, order.getOrderNo());
			if (appAccountDao.settleFrozenCoin(order.getUid(), amt) <= 0) {
				throw new BaseException("settle frozen coin failed");
			}
		} catch (Exception e) {
			log.error("withdraw settle failed orderNo={}", orderNo, e);
			TransactionUtils.markRollbackOnly();
			throw new BaseException("withdraw settle failed", e);
		}
		log.info("withdraw success orderNo={} uid={} amt={}", orderNo, order.getUid(), amt);
	}

	/** OnePay 失败：只解冻，不扣 coin_balance */
	@Transactional(rollbackFor = Exception.class)
	public void failAndUnfreeze(String orderNo, String failReason) {
		UserWithdrawOrderEntity order = loadPayingOrPending(orderNo);
		if (order == null) {
			return;
		}
		int fromStatus = order.getStatus();
		int failed = userWithdrawOrderDao.casFinish(order.getId(), fromStatus,
				WithdrawOrderStatusEnums.FAILED.getCode(), null, failReason);
		if (failed <= 0) {
			return;
		}
		int amt = nvlPoints(order.getPointsAmt());
		try {
			if (amt > 0 && appAccountDao.unfreezeCoinBalance(order.getUid(), amt) <= 0) {
				throw new BaseException("unfreeze coin failed");
			}
			userWithdrawOrderDao.casStatus(order.getId(),
					WithdrawOrderStatusEnums.FAILED.getCode(),
					WithdrawOrderStatusEnums.REFUNDED.getCode());
		} catch (Exception e) {
			log.error("withdraw unfreeze failed orderNo={}", orderNo, e);
			TransactionUtils.markRollbackOnly();
			throw new BaseException("withdraw unfreeze failed", e);
		}
		log.info("withdraw failed and unfrozen orderNo={} uid={} amt={}", orderNo, order.getUid(), amt);
	}

	private UserWithdrawOrderEntity loadPayingOrPending(String orderNo) {
		if (StringUtils.isEmpty(orderNo)) {
			return null;
		}
		UserWithdrawOrderEntity order = userWithdrawOrderDao.findByOrderNo(orderNo);
		if (order == null) {
			return null;
		}
		Integer status = order.getStatus();
		boolean canFinish = Integer.valueOf(WithdrawOrderStatusEnums.PAYING.getCode()).equals(status)
				|| Integer.valueOf(WithdrawOrderStatusEnums.PENDING.getCode()).equals(status);
		if (!canFinish) {
			return null;
		}
		return order;
	}

	private boolean callOnePay(UserWithdrawOrderEntity order) {
		OnePayWithdrawPayoutRequest req = new OnePayWithdrawPayoutRequest();
		req.setOrderNo(order.getOrderNo());
		req.setOnePayAccount(order.getWalletAddress());
		AppAccountEntity account = appAccountDao.findByUid(order.getUid());
		req.setOnePayOpenId(account == null ? null : account.getOnepayOpenId());
		req.setPoints(nvlPoints(order.getPointsAmt()));
		try {
			log.info("onepay withdraw request orderNo={} uid={} points={}",
					order.getOrderNo(), order.getUid(), req.getPoints());
			ResponseEntity<String> result = restTemplate.postForEntity(
					onePayProperties.getWithdrawUrl(), req, String.class);
			if (result.getStatusCode() != HttpStatus.OK) {
				log.warn("onepay withdraw rejected orderNo={} status={}",
						order.getOrderNo(), result.getStatusCode());
				return false;
			}
			return true;
		} catch (Exception e) {
			log.error("onepay withdraw http failed orderNo={}", order.getOrderNo(), e);
			return false;
		}
	}

	private void writeWithdrawLedger(Integer uid, int amt, String orderNo) throws Exception {
		if (uid == null || amt <= 0) {
			return;
		}
		String bizType = CoinBizTypeEnums.WITHDRAW.getName();
		String bizId = "WITHDRAW:" + orderNo;
		UserCoinLedgerEntity exist = userCoinLedgerDao.findByBiz(uid, bizType, bizId);
		if (exist != null) {
			return;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		long before = account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt(-amt);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before - amt);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setTaskCode("");
		ledger.setAdBoostFlag(0);
		ledger.setRemark("提现扣减");
		GenericityUtil.setDate(ledger);
		try {
			userCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			log.warn("withdraw ledger duplicate uid={} bizId={}", uid, bizId);
		}
	}

	private static int nvlPoints(Integer points) {
		return points == null ? 0 : points;
	}
}
