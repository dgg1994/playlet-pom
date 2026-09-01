package com.playlet.internal.service.support;

import com.playlet.internal.api.request.BankcardRechargeRequest;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.dao.wallet.WalletBankcardDao;
import com.playlet.internal.dao.wallet.WalletCardTransactionDao;
import com.playlet.internal.dao.wallet.WalletUserDao;
import com.playlet.internal.entity.welfare.UserWithdrawOrderEntity;
import com.playlet.internal.entity.wallet.WalletBankcardEntity;
import com.playlet.internal.entity.wallet.WalletCardTransactionEntity;
import com.playlet.internal.entity.wallet.WalletUserEntity;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.third.ThirdService;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.WalletRequestOrderIdSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 提现打款：向 U 卡发起充值并落本地流水，结果由 Webhook 回写。
 */
@Slf4j
@Component
public class WithdrawWalletRechargeSupport {

	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletBankcardDao walletBankcardDao;
	@Autowired
	private WalletCardTransactionDao walletCardTransactionDao;
	@Autowired
	private ThirdService thirdService;

	/** 提交 U 卡充值打款 */
	public boolean submitPayout(UserWithdrawOrderEntity order) {
		if (order == null || order.getUid() == null || order.getUserType() == null) {
			return false;
		}
		if (order.getTargetUserBankcardId() == null) {
			log.warn("withdraw wallet payout missing target orderNo={}", order.getOrderNo());
			return false;
		}
		WalletUserEntity user = walletUserDao.findByLocal(order.getUserType(), order.getUid());
		if (user == null) {
			log.warn("withdraw wallet payout user not found orderNo={} userType={} uid={}",
					order.getOrderNo(), order.getUserType(), order.getUid());
			return false;
		}
		WalletBankcardEntity card = walletBankcardDao.findByUserBankcardId(order.getTargetUserBankcardId());
		if (card == null || !user.getId().equals(card.getWalletUserId())) {
			log.warn("withdraw wallet payout card not found orderNo={} userBankcardId={}",
					order.getOrderNo(), order.getTargetUserBankcardId());
			return false;
		}
		int amount = resolveRechargeAmount(order);
		if (amount <= 0) {
			log.warn("withdraw wallet payout invalid amount orderNo={} amount={}", order.getOrderNo(), amount);
			return false;
		}
		BankcardRechargeRequest req = new BankcardRechargeRequest();
		req.setUserBankcardId(order.getTargetUserBankcardId());
		req.setAmount(amount);
		String requestOrderId = WalletRequestOrderIdSupport.resolve(order.getRequestOrderId(),
				WalletConstants.REQUEST_ORDER_PREFIX_CARD_RECHARGE, user.getWalletUid());
		req.setRequestOrderId(requestOrderId);
		try {
			thirdService.rechargeBankcard(user.getWalletUid(), req);
			insertRechargeTransaction(user, card, order, req);
			log.info("withdraw wallet payout submitted orderNo={} requestOrderId={} amount={}",
					order.getOrderNo(), order.getRequestOrderId(), amount);
			return true;
		} catch (BaseException e) {
			log.error("withdraw wallet payout rejected orderNo={} requestOrderId={}",
					order.getOrderNo(), order.getRequestOrderId(), e);
			return false;
		} catch (Exception e) {
			log.error("withdraw wallet payout error orderNo={} requestOrderId={}",
					order.getOrderNo(), order.getRequestOrderId(), e);
			return false;
		}
	}

	private void insertRechargeTransaction(WalletUserEntity user, WalletBankcardEntity card,
			UserWithdrawOrderEntity order, BankcardRechargeRequest query) {
		Date now = new Date();
		BigDecimal amount = new BigDecimal(query.getAmount());
		String currency = StringUtils.isEmpty(card.getCurrency())
				? WalletConstants.DEFAULT_CURRENCY : card.getCurrency();
		WalletCardTransactionEntity txn = new WalletCardTransactionEntity();
		txn.setWalletUserId(user.getId());
		txn.setWalletUid(user.getWalletUid());
		txn.setWalletBankcardId(card.getId());
		txn.setUserBankcardId(card.getUserBankcardId());
		txn.setCardProductId(card.getCardProductId());
		txn.setCardUuid(card.getCardUuid());
		txn.setCardNo(card.getCardNo());
		txn.setRequestOrderId(query.getRequestOrderId());
		txn.setWithdrawOrderId(order.getId());
		txn.setBizType(WalletConstants.BIZ_RECHARGE);
		txn.setTransType(WalletConstants.TRANS_TOPUP);
		txn.setOrderState(WalletConstants.ORDER_STATE_PENDING);
		txn.setOrderStateName(WalletConstants.ORDER_STATE_PENDING_NAME);
		txn.setLocalCurrency(currency);
		txn.setLocalCurrencyAmt(amount);
		txn.setTransCurrency(currency);
		txn.setTransCurrencyAmt(amount);
		txn.setTitle("提现打款");
		txn.setSetTime(now);
		txn.setGmtModified(now);
		try {
			walletCardTransactionDao.insert(txn);
		} catch (DuplicateKeyException e) {
			log.warn("withdraw wallet payout txn duplicate requestOrderId={}", query.getRequestOrderId(), e);
		} catch (Exception e) {
			log.error("withdraw wallet payout txn insert failed requestOrderId={}", query.getRequestOrderId(), e);
			throw new BaseException("withdraw wallet txn insert failed", e);
		}
	}

	private static int resolveRechargeAmount(UserWithdrawOrderEntity order) {
		if (order.getActualAmt() != null && order.getActualAmt().signum() > 0) {
			return order.getActualAmt().intValue();
		}
		return order.getPointsAmt() == null ? 0 : order.getPointsAmt();
	}
}
