package com.playlet.internal.service.impl;

import com.playlet.internal.dao.welfare.UserWithdrawOrderDao;
import com.playlet.internal.entity.welfare.UserWithdrawOrderEntity;
import com.playlet.internal.enums.WithdrawOrderStatusEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.WithdrawPayoutService;
import com.playlet.internal.service.support.WithdrawWalletHandler;
import com.playlet.internal.service.support.WithdrawWalletHandlerRegistry;
import com.playlet.internal.service.support.WithdrawWalletRechargeSupport;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.TransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * U 卡提现打款：提交后只冻结；Webhook 确认充值结果后再解冻并扣减。
 */
@Slf4j
@Service
public class WithdrawPayoutServiceImpl implements WithdrawPayoutService {

	@Value("${withdraw.mock-success:true}")
	private boolean mockSuccess;

	@Autowired
	private UserWithdrawOrderDao userWithdrawOrderDao;
	@Autowired
	private WithdrawWalletHandlerRegistry walletHandlerRegistry;
	@Autowired
	private WithdrawWalletRechargeSupport withdrawWalletRechargeSupport;

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
			if (mockSuccess) {
				self.confirmSuccess(order.getOrderNo(), "MOCK_" + order.getOrderNo(), null);
				return;
			}
			if (!withdrawWalletRechargeSupport.submitPayout(order)) {
				self.failAndUnfreeze(order.getOrderNo(), "wallet recharge reject");
			}
		} catch (Exception e) {
			log.error("withdraw payout failed orderId={}", orderId, e);
			UserWithdrawOrderEntity order = userWithdrawOrderDao.selectById(orderId);
			if (order != null && !StringUtils.isEmpty(order.getOrderNo())) {
				try {
					self.failAndUnfreeze(order.getOrderNo(), "wallet payout error");
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

	/** 充值确认到账：解冻并扣减金币 */
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
		WithdrawWalletHandler handler = walletHandlerRegistry.of(order.getUserType());
		try {
			handler.writeWithdrawLedger(order.getUid(), amt, order.getOrderNo());
			if (handler.settleFrozen(order.getUid(), amt) <= 0) {
				throw new BaseException("settle frozen coin failed");
			}
		} catch (Exception e) {
			log.error("withdraw settle failed orderNo={} userType={}", orderNo, order.getUserType(), e);
			TransactionUtils.markRollbackOnly();
			throw new BaseException("withdraw settle failed", e);
		}
		log.info("withdraw success orderNo={} userType={} uid={} amt={}",
				orderNo, order.getUserType(), order.getUid(), amt);
	}

	/** 打款失败：只解冻，不扣 coin_balance */
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
		WithdrawWalletHandler handler = walletHandlerRegistry.of(order.getUserType());
		try {
			if (amt > 0 && handler.unfreeze(order.getUid(), amt) <= 0) {
				throw new BaseException("unfreeze coin failed");
			}
			handler.writeWithdrawRefundLedger(order.getUid(), amt, order.getOrderNo());
			userWithdrawOrderDao.casStatus(order.getId(),
					WithdrawOrderStatusEnums.FAILED.getCode(),
					WithdrawOrderStatusEnums.REFUNDED.getCode());
		} catch (Exception e) {
			log.error("withdraw unfreeze failed orderNo={} userType={}", orderNo, order.getUserType(), e);
			TransactionUtils.markRollbackOnly();
			throw new BaseException("withdraw unfreeze failed", e);
		}
		log.info("withdraw failed and unfrozen orderNo={} userType={} uid={} amt={}",
				orderNo, order.getUserType(), order.getUid(), amt);
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

	private static int nvlPoints(Integer points) {
		return points == null ? 0 : points;
	}
}
