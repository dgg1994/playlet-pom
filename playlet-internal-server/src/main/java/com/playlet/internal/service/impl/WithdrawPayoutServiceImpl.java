package com.playlet.internal.service.impl;

import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.welfare.UserCoinLedgerDao;
import com.playlet.internal.dao.welfare.UserWithdrawOrderDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.welfare.UserCoinLedgerEntity;
import com.playlet.internal.entity.welfare.UserWithdrawOrderEntity;
import com.playlet.internal.enums.CoinBizTypeEnums;
import com.playlet.internal.enums.WithdrawOrderStatusEnums;
import com.playlet.internal.service.WithdrawPayoutService;
import com.playlet.internal.utils.GenericityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * P0 Mock 打款：可配置成功/失败，失败原路退币。
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

	@Override
	@Async("asyncExecutor")
	public void payoutAsync(Long orderId) {
		if (orderId == null) {
			return;
		}
		try {
			Thread.sleep(300L);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		try {
			doPayout(orderId);
		} catch (Exception e) {
			log.error("withdraw payout failed orderId={}", orderId, e);
		}
	}

	@Transactional
	public void doPayout(Long orderId) throws Exception {
		UserWithdrawOrderEntity order = userWithdrawOrderDao.selectById(orderId);
		if (order == null) {
			return;
		}
		if (!Integer.valueOf(WithdrawOrderStatusEnums.PENDING.getCode()).equals(order.getStatus())) {
			return;
		}
		int moved = userWithdrawOrderDao.casStatus(orderId,
				WithdrawOrderStatusEnums.PENDING.getCode(),
				WithdrawOrderStatusEnums.PAYING.getCode());
		if (moved <= 0) {
			return;
		}
		if (mockSuccess) {
			String txHash = "MOCK_" + order.getOrderNo();
			userWithdrawOrderDao.casFinish(orderId,
					WithdrawOrderStatusEnums.PAYING.getCode(),
					WithdrawOrderStatusEnums.SUCCESS.getCode(),
					txHash, null);
			log.info("withdraw mock success orderNo={} txHash={}", order.getOrderNo(), txHash);
			return;
		}
		String reason = "mock payout failed";
		int failed = userWithdrawOrderDao.casFinish(orderId,
				WithdrawOrderStatusEnums.PAYING.getCode(),
				WithdrawOrderStatusEnums.FAILED.getCode(),
				null, reason);
		if (failed <= 0) {
			return;
		}
		refund(order);
		userWithdrawOrderDao.casStatus(orderId,
				WithdrawOrderStatusEnums.FAILED.getCode(),
				WithdrawOrderStatusEnums.REFUNDED.getCode());
		log.info("withdraw mock failed and refunded orderNo={}", order.getOrderNo());
	}

	private void refund(UserWithdrawOrderEntity order) throws Exception {
		Integer uid = order.getUid();
		int amt = order.getPointsAmt() == null ? 0 : order.getPointsAmt();
		if (uid == null || amt <= 0) {
			return;
		}
		String bizType = CoinBizTypeEnums.WITHDRAW_REFUND.getName();
		String bizId = "WITHDRAW_REFUND:" + order.getOrderNo();
		UserCoinLedgerEntity exist = userCoinLedgerDao.findByBiz(uid, bizType, bizId);
		if (exist != null) {
			return;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		long before = account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt(amt);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before + amt);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setTaskCode("");
		ledger.setAdBoostFlag(0);
		ledger.setRemark("提现失败退回");
		GenericityUtil.setDate(ledger);
		try {
			userCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			return;
		}
		appAccountDao.addCoinBalance(uid, amt);
	}
}
