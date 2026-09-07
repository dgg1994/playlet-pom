package com.playlet.oversea.service.support;

import com.playlet.oversea.api.request.WalletWebhookNotifyRequest;
import com.playlet.oversea.constants.WalletConstants;
import com.playlet.oversea.constants.WalletNotifyConstants;
import com.playlet.oversea.dao.wallet.WalletAccountDao;
import com.playlet.oversea.dao.wallet.WalletBankcardDao;
import com.playlet.oversea.dao.wallet.WalletCardCloseDao;
import com.playlet.oversea.dao.wallet.WalletCardTransactionDao;
import com.playlet.oversea.dao.wallet.WalletLogDao;
import com.playlet.oversea.entity.wallet.WalletAccountEntity;
import com.playlet.oversea.entity.wallet.WalletBankcardEntity;
import com.playlet.oversea.entity.wallet.WalletCardCloseEntity;
import com.playlet.oversea.entity.wallet.WalletCardTransactionEntity;
import com.playlet.oversea.entity.wallet.WalletLogEntity;
import com.playlet.oversea.entity.wallet.WalletUserEntity;
import com.playlet.oversea.enums.WalletCardCloseReviewStatusEnums;
import com.playlet.oversea.enums.WalletCardStatusEnums;
import com.playlet.oversea.enums.WalletLogOperateTypeEnums;
import com.playlet.oversea.enums.WalletLogStatusEnums;
import com.playlet.oversea.enums.WalletLogTradeTypeEnums;
import com.playlet.oversea.enums.WalletNotifyEventEnums;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.OrderCodeFactory;
import com.playlet.oversea.utils.StringUtils;
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
	private WalletCardCloseDao walletCardCloseDao;
	@Autowired
	private WalletLogDao walletLogDao;
	@Autowired
	private WalletBankcardSyncSupport walletBankcardSyncSupport;
	@Autowired
	private WalletNotifyService walletNotifyService;

	/**
	 * 卡片关闭回调：退款金额取销卡申请时快照 balance（对齐 onetoken），回写 refundAmt 后入钱包。
	 */
	@Transactional(rollbackFor = Exception.class)
	public void handleCardClose(WalletWebhookNotifyRequest body, WalletBankcardEntity card,
			WalletUserEntity user) {
		if (card == null) {
			return;
		}
		persistCardNo(body, card);
		WalletCardCloseEntity close = walletCardCloseDao.findByUserBankcardId(card.getUserBankcardId());
		// 对齐 onetoken：refundAmt = 申请记录 balance，不用 webhook.refundAmount
		BigDecimal refund = resolveRefundFromCloseSnapshot(close, card);
		String requestOrderId = orderNoFromBody(body);
		boolean alreadyClosed = Integer.valueOf(WalletCardStatusEnums.CLOSED.getCode())
				.equals(card.getCardStatus());
		if (alreadyClosed) {
			// 幂等：仅补齐销卡记录成功态，避免重复退款
			markCloseRecordSuccess(user, card, close, refund, requestOrderId);
			log.info("wallet webhook card close skip already closed userBankcardId={}",
					card.getUserBankcardId());
			return;
		}
		walletBankcardDao.updateBalance(card.getId(), BigDecimal.ZERO);
		walletBankcardDao.updateCardStatus(card.getId(),
				WalletCardStatusEnums.CLOSED.getCode(), WalletCardStatusEnums.CLOSED.getLabel());
		// 用户发起时已写入处理中记录：refundAmt=balance，状态成功
		markCloseRecordSuccess(user, card, close, refund, requestOrderId);
		if (refund.compareTo(BigDecimal.ZERO) <= 0 || user == null) {
			log.info("wallet webhook card close no refund userBankcardId={} refund={}",
					card.getUserBankcardId(), refund);
			walletNotifyService.notify(user, WalletNotifyEventEnums.CARD_CLOSE,
					"wallet:card:close:" + card.getUserBankcardId(),
					WalletNotifyConstants.JUMP_CARD, String.valueOf(card.getId()));
			return;
		}
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
		if (account == null) {
			log.warn("wallet webhook card close account missing walletUserId={}", user.getId());
			walletNotifyService.notify(user, WalletNotifyEventEnums.CARD_CLOSE,
					"wallet:card:close:" + card.getUserBankcardId(),
					WalletNotifyConstants.JUMP_CARD, String.valueOf(card.getId()));
			return;
		}
		BigDecimal walletBefore = nz(account.getAvailableBalance());
		int rows = walletAccountDao.addAvailableBalance(account.getId(), refund);
		if (rows <= 0) {
			throw new BaseException(I18nUtil.getMessage("base_error"));
		}
		String orderNo = OrderCodeFactory.getOrderCode(card.getUserBankcardId());
		insertCloseCardTransaction(card, refund, orderNo);
		insertCloseWalletLog(user, account, card, refund, walletBefore, orderNo);
		walletNotifyService.notify(user, WalletNotifyEventEnums.CARD_CLOSE,
				"wallet:card:close:" + card.getUserBankcardId(),
				WalletNotifyConstants.JUMP_CARD, String.valueOf(card.getId()));
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

	/**
	 * 退款金额：优先销卡申请快照 balance（onetoken）；无申请记录时回退当前卡余额。
	 */
	private static BigDecimal resolveRefundFromCloseSnapshot(WalletCardCloseEntity close,
			WalletBankcardEntity card) {
		if (close != null && close.getBalance() != null) {
			return nz(close.getBalance());
		}
		return nz(card == null ? null : card.getBalance());
	}

	private void insertCloseCardTransaction(WalletBankcardEntity card, BigDecimal refund, String orderNo) {
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
		txn.setOrderState(WalletLogStatusEnums.POSTED.getIntCode());
		txn.setOrderStateName(WalletLogStatusEnums.POSTED.getLabel());
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

	private static BigDecimal nz(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	/**
	 * Webhook 成功：refundAmt = 申请快照 balance，状态改为成功（对齐 onetoken）。
	 */
	private void markCloseRecordSuccess(WalletUserEntity user, WalletBankcardEntity card,
			WalletCardCloseEntity close, BigDecimal refund, String requestOrderId) {
		if (card == null || card.getUserBankcardId() == null) {
			return;
		}
		Date now = new Date();
		if (close == null) {
			if (user == null) {
				log.warn("wallet card close record missing and user null userBankcardId={}",
						card.getUserBankcardId());
				return;
			}
			close = new WalletCardCloseEntity();
			close.setWalletUserId(user.getId());
			close.setWalletUid(user.getWalletUid());
			close.setCardProductId(card.getCardProductId());
			close.setCardUuid(card.getCardUuid());
			close.setCardType(card.getBankcardNature());
			close.setCardNo(card.getCardNo());
			close.setUserBankcardId(card.getUserBankcardId());
			close.setBalance(refund);
			close.setRefundAmt(refund);
			close.setRequestOrderId(requestOrderId);
			close.setReviewStatus(WalletCardCloseReviewStatusEnums.SUCCESS.getIndex());
			close.setSetTime(now);
			close.setGmtModified(now);
			try {
				walletCardCloseDao.insert(close);
			} catch (Exception e) {
				log.error("wallet card close record insert failed userBankcardId={}",
						card.getUserBankcardId(), e);
				throw new BaseException(I18nUtil.getMessage("base_error"), e);
			}
			return;
		}
		if (WalletCardCloseReviewStatusEnums.SUCCESS.getIndex().equals(close.getReviewStatus())) {
			return;
		}
		// 对齐 onetoken：setRefundAmt(cardCloseEntity.getBalance())
		close.setRefundAmt(refund);
		if (!StringUtils.isEmpty(requestOrderId)) {
			close.setRequestOrderId(requestOrderId);
		}
		if (!StringUtils.isEmpty(card.getCardNo())) {
			close.setCardNo(card.getCardNo());
		}
		close.setReviewStatus(WalletCardCloseReviewStatusEnums.SUCCESS.getIndex());
		close.setGmtModified(now);
		try {
			walletCardCloseDao.updateById(close);
		} catch (Exception e) {
			log.error("wallet card close record update failed id={} userBankcardId={}",
					close.getId(), card.getUserBankcardId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private static String orderNoFromBody(WalletWebhookNotifyRequest body) {
		if (body == null) {
			return null;
		}
		return body.getOrderId();
	}
}
