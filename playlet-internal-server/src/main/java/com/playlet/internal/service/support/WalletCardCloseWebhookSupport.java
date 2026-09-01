package com.playlet.internal.service.support;

import com.playlet.internal.api.request.WalletWebhookNotifyRequest;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.dao.wallet.WalletAccountDao;
import com.playlet.internal.dao.wallet.WalletBankcardDao;
import com.playlet.internal.dao.wallet.WalletCardTransactionDao;
import com.playlet.internal.dao.wallet.WalletLogDao;
import com.playlet.internal.entity.wallet.WalletAccountEntity;
import com.playlet.internal.entity.wallet.WalletBankcardEntity;
import com.playlet.internal.entity.wallet.WalletCardTransactionEntity;
import com.playlet.internal.entity.wallet.WalletLogEntity;
import com.playlet.internal.entity.wallet.WalletUserEntity;
import com.playlet.internal.enums.WalletCardStatusEnums;
import com.playlet.internal.enums.WalletLogOperateTypeEnums;
import com.playlet.internal.enums.WalletLogStatusEnums;
import com.playlet.internal.enums.WalletLogTradeTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.OrderCodeFactory;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 卡注销 Webhook 处理（对齐 onetoken WebhookServiceImpl.closeCardRes）。
 */
@Slf4j
@Service
public class WalletCardCloseWebhookSupport {

	@Autowired
	private WalletBankcardDao walletBankcardDao;
	@Autowired
	private WalletAccountDao walletAccountDao;
	@Autowired
	private WalletCardTransactionDao walletCardTransactionDao;
	@Autowired
	private WalletLogDao walletLogDao;
	@Autowired
	private WalletBankcardSyncSupport walletBankcardSyncSupport;

	/**
	 * 卡片关闭回调：更新卡状态、余额退回钱包并落流水。
	 */
	@Transactional(rollbackFor = Exception.class)
	public void handleCardClose(WalletWebhookNotifyRequest body, WalletBankcardEntity card,
			WalletUserEntity user) {
		if (card == null) {
			return;
		}
		if (Integer.valueOf(WalletCardStatusEnums.CLOSED.getCode()).equals(card.getCardStatus())) {
			log.info("wallet webhook card close skip already closed userBankcardId={}",
					card.getUserBankcardId());
			return;
		}
		persistCardNo(body, card);
		BigDecimal refund = resolveRefundAmount(body, card);
		BigDecimal cardBalanceBefore = nz(card.getBalance());
		walletBankcardDao.updateBalance(card.getId(), BigDecimal.ZERO);
		walletBankcardDao.updateCardStatus(card.getId(),
				WalletCardStatusEnums.CLOSED.getCode(), WalletCardStatusEnums.CLOSED.getLabel());
		if (refund.compareTo(BigDecimal.ZERO) <= 0 || user == null) {
			log.info("wallet webhook card close no refund userBankcardId={} refund={}",
					card.getUserBankcardId(), refund);
			return;
		}
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
		if (account == null) {
			log.warn("wallet webhook card close account missing walletUserId={}", user.getId());
			return;
		}
		BigDecimal walletBefore = nz(account.getAvailableBalance());
		int rows = walletAccountDao.addAvailableBalance(account.getId(), refund);
		if (rows <= 0) {
			throw new BaseException(I18nUtil.getMessage("base_error"));
		}
		String orderNo = OrderCodeFactory.getOrderCode(card.getUserBankcardId());
		insertCloseCardTransaction(card, refund, cardBalanceBefore, orderNo);
		insertCloseWalletLog(user, account, card, refund, walletBefore, orderNo);
		log.info("wallet webhook card close refunded userBankcardId={} refund={} walletUserId={}",
				card.getUserBankcardId(), refund, user.getId());
	}

	private void persistCardNo(WalletWebhookNotifyRequest body, WalletBankcardEntity card) {
		if (!StringUtils.isEmpty(body.getCardNo())) {
			walletBankcardDao.updateCardNo(card.getId(), body.getCardNo());
			card.setCardNo(body.getCardNo());
			return;
		}
		walletBankcardSyncSupport.syncCardNo(card);
	}

	private static BigDecimal resolveRefundAmount(WalletWebhookNotifyRequest body, WalletBankcardEntity card) {
		BigDecimal refund = parseAmount(body.getRefundAmount());
		if (refund != null && refund.compareTo(BigDecimal.ZERO) > 0) {
			return refund;
		}
		return nz(card.getBalance());
	}

	private void insertCloseCardTransaction(WalletBankcardEntity card, BigDecimal refund,
			BigDecimal cardBalanceBefore, String orderNo) {
		Date now = new Date();
		String currency = StringUtils.isEmpty(card.getCurrency())
				? WalletConstants.DEFAULT_CURRENCY : card.getCurrency();
		WalletCardTransactionEntity txn = new WalletCardTransactionEntity();
		txn.setWalletUserId(card.getWalletUserId());
		txn.setWalletUid(card.getWalletUid());
		txn.setWalletBankcardId(card.getId());
		txn.setUserBankcardId(card.getUserBankcardId());
		txn.setCardProductId(card.getCardProductId());
		txn.setCardUuid(card.getCardUuid());
		txn.setCardNo(card.getCardNo());
		txn.setRequestOrderId(orderNo);
		txn.setThirdOrderNum(orderNo);
		txn.setBizType(WalletConstants.BIZ_CLOSE);
		txn.setTransType(WalletConstants.BIZ_CLOSE);
		txn.setOrderState(WalletConstants.ORDER_STATE_SUCCESS);
		txn.setOrderStateName(WalletConstants.ORDER_STATE_SUCCESS_NAME);
		txn.setLocalCurrency(currency);
		txn.setLocalCurrencyAmt(refund.negate());
		txn.setTransCurrency(currency);
		txn.setTransCurrencyAmt(refund.negate());
		txn.setHandlingFees(BigDecimal.ZERO);
		txn.setTitle(I18nUtil.getMessage("wallet.log.card_close"));
		txn.setSetTime(now);
		txn.setGmtModified(now);
		walletCardTransactionDao.insert(txn);
	}

	private void insertCloseWalletLog(WalletUserEntity user, WalletAccountEntity account,
			WalletBankcardEntity card, BigDecimal refund, BigDecimal walletBefore, String orderNo) {
		Date now = new Date();
		WalletLogEntity logEntity = new WalletLogEntity();
		logEntity.setOrderNo(orderNo);
		logEntity.setOutOrderNo(orderNo);
		logEntity.setWalletUserId(user.getId());
		logEntity.setWalletUid(user.getWalletUid());
		logEntity.setTradeType(WalletLogTradeTypeEnums.INCOME.getCode());
		logEntity.setTitle(I18nUtil.getMessage("wallet.log.card_close"));
		logEntity.setPrimevalMoney(walletBefore);
		logEntity.setPrimevalMoneyUnit(WalletConstants.DEFAULT_CURRENCY);
		logEntity.setRealMoney(refund);
		logEntity.setServiceCharge(BigDecimal.ZERO);
		logEntity.setFormName(card.getCardNo());
		logEntity.setFormAccount(card.getCardNo());
		logEntity.setToName(user.getEmail());
		logEntity.setToAccount(user.getEmail());
		logEntity.setWalletBankcardId(card.getId());
		logEntity.setStatus(WalletLogStatusEnums.POSTED.getCode());
		logEntity.setOperateType(WalletLogOperateTypeEnums.CARD_CLOSE.getCode());
		logEntity.setSetTime(now);
		logEntity.setGmtModified(now);
		walletLogDao.insert(logEntity);
	}

	private static BigDecimal parseAmount(String amount) {
		if (StringUtils.isEmpty(amount)) {
			return null;
		}
		try {
			return new BigDecimal(amount.trim());
		} catch (Exception e) {
			return null;
		}
	}

	private static BigDecimal nz(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}
}
