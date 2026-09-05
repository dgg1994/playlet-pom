package com.playlet.internal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playlet.internal.api.request.WalletWebhookNotifyRequest;
import com.playlet.internal.api.response.WalletWebhookNotifyResponse;
import com.playlet.internal.api.response.ThirdBankcardBalanceResp;
import com.playlet.internal.api.response.ThirdBankcardInfoResp;
import com.playlet.internal.config.ThirdPartyProperties;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.constants.WalletNotifyConstants;
import com.playlet.internal.constants.WalletWebhookConstants;
import com.playlet.internal.dao.welfare.UserWithdrawOrderDao;
import com.playlet.internal.dao.wallet.WalletBankcardDao;
import com.playlet.internal.dao.wallet.WalletCardApplyDao;
import com.playlet.internal.dao.wallet.WalletCardTransactionDao;
import com.playlet.internal.dao.wallet.WalletLogDao;
import com.playlet.internal.dao.wallet.WalletUserDao;
import com.playlet.internal.dao.wallet.WalletWebhookEventDao;
import com.playlet.internal.entity.welfare.UserWithdrawOrderEntity;
import com.playlet.internal.entity.wallet.WalletBankcardEntity;
import com.playlet.internal.entity.wallet.WalletCardApplyEntity;
import com.playlet.internal.entity.wallet.WalletCardTransactionEntity;
import com.playlet.internal.entity.wallet.WalletLogEntity;
import com.playlet.internal.entity.wallet.WalletUserEntity;
import com.playlet.internal.entity.wallet.WalletWebhookEventEntity;
import com.playlet.internal.enums.WalletCardApplyStateEnums;
import com.playlet.internal.enums.WalletCardStatusEnums;
import com.playlet.internal.enums.WalletKycStateEnums;
import com.playlet.internal.enums.WalletLogStatusEnums;
import com.playlet.internal.enums.WalletNotifyEventEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.WalletWebhookService;
import com.playlet.internal.service.WithdrawPayoutService;
import com.playlet.internal.service.support.WalletBankcardSyncSupport;
import com.playlet.internal.service.support.WalletCardCloseWebhookSupport;
import com.playlet.internal.service.support.WalletNotifyService;
import com.playlet.internal.service.support.WalletOpenCardSettlementService;
import com.playlet.internal.service.third.ThirdService;
import com.playlet.internal.service.third.WalletUserService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.RsaVerifyUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;

/**
 * worldPay WebHook 回调：验签、幂等、按 eventType 更新本地钱包数据。
 */
@Slf4j
@RestController
@CrossOrigin
public class WalletWebhookServiceImpl implements WalletWebhookService {

	private static final String HEADER_APP_ID = "appId";
	private static final String HEADER_NONCE = "nonce";
	private static final String HEADER_TIMESTAMP = "timestamp";
	private static final String HEADER_SIGN = "sign";

	@Autowired
	private ThirdPartyProperties thirdPartyProperties;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private WalletWebhookEventDao walletWebhookEventDao;
	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletBankcardDao walletBankcardDao;
	@Autowired
	private WalletCardApplyDao walletCardApplyDao;
	@Autowired
	private WalletCardTransactionDao walletCardTransactionDao;
	@Autowired
	private WalletLogDao walletLogDao;
	@Autowired
	private UserWithdrawOrderDao userWithdrawOrderDao;
	@Autowired
	private WalletUserService walletUserService;
	@Autowired
	private ThirdService thirdService;
	@Autowired
	private WithdrawPayoutService withdrawPayoutService;
	@Autowired
	private WalletOpenCardSettlementService walletOpenCardSettlementService;
	@Autowired
	private WalletBankcardSyncSupport walletBankcardSyncSupport;
	@Autowired
	private WalletCardCloseWebhookSupport walletCardCloseWebhookSupport;
	@Autowired
	private WalletNotifyService walletNotifyService;

	@Lazy
	@Autowired
	private WalletWebhookServiceImpl self;

	@Override
	public WalletWebhookNotifyResponse notify(@RequestBody WalletWebhookNotifyRequest body,
			HttpServletRequest request) {
		if (body == null || StringUtils.isEmpty(body.getEventType())) {
			log.warn("wallet webhook invalid payload");
			return WalletWebhookNotifyResponse.fail(Constants.HTTP_RES_CODE_500, "invalid payload");
		}
		String eventId = body.getEventId();
		if (StringUtils.isEmpty(eventId)) {
			log.warn("wallet webhook missing eventId eventType={}", body.getEventType());
			return WalletWebhookNotifyResponse.fail(Constants.HTTP_RES_CODE_500, "eventId required");
		}
//		if (!verifyInboundSign(body, request)) {
//			log.warn("wallet webhook sign verify failed eventId={} eventType={}", eventId, body.getEventType());
//			return WalletWebhookNotifyResponse.fail(Constants.HTTP_RES_CODE_403, "sign verify failed");
//		}

		WalletWebhookEventEntity existed = walletWebhookEventDao.findByEventId(eventId);
		if (existed != null && Integer.valueOf(WalletWebhookConstants.PROCESS_SUCCESS)
				.equals(existed.getProcessStatus())) {
			log.info("wallet webhook duplicate ack eventId={} eventType={}", eventId, body.getEventType());
			return WalletWebhookNotifyResponse.success();
		}

		WalletWebhookEventEntity eventRow = existed != null ? existed : insertEvent(body);
		try {
			dispatch(body);
			markProcessResult(eventRow.getId(), WalletWebhookConstants.PROCESS_SUCCESS, "ok");
			log.info("wallet webhook handled eventId={} eventType={}", eventId, body.getEventType());
			return WalletWebhookNotifyResponse.success();
		} catch (Exception e) {
			log.error("wallet webhook handle failed eventId={} eventType={}", eventId, body.getEventType(), e);
			markProcessResult(eventRow.getId(), WalletWebhookConstants.PROCESS_FAILED, truncateMsg(e.getMessage()));
			return WalletWebhookNotifyResponse.fail(Constants.HTTP_RES_CODE_500, "process failed");
		}
	}

	/** 按 eventType 分发业务处理 */
	private void dispatch(WalletWebhookNotifyRequest body) {
		String eventType = body.getEventType();
		if (WalletWebhookConstants.EVENT_KYC_STATUS_CHANGE.equals(eventType)) {
			handleKycStatusChange(body);
			return;
		}
		if (WalletWebhookConstants.EVENT_CARD_RECHARGE_RESULT.equals(eventType)) {
			self.handleCardRechargeResult(body);
			return;
		}
		if (WalletWebhookConstants.EVENT_CARD_STATUS_CHANGE.equals(eventType)) {
			handleCardStatusChange(body);
			return;
		}
		if (WalletWebhookConstants.EVENT_TRANSACTION_CREATED.equals(eventType)) {
			self.handleTransactionCreated(body);
			return;
		}
		if (WalletWebhookConstants.EVENT_3DS.equals(eventType)) {
			handle3ds(body);
			return;
		}
		if (WalletWebhookConstants.EVENT_MERCHANT_RECHARGE.equals(eventType)) {
			handleMerchantRecharge(body);
			return;
		}
		log.info("wallet webhook ignore unknown eventType={} eventId={}", eventType, body.getEventId());
	}

	/** KYC 状态变更：回写 wallet_account / wallet_kyc_apply */
	private void handleKycStatusChange(WalletWebhookNotifyRequest body) {
		if (body.getUid() == null) {
			throw new BaseException("kyc webhook uid empty");
		}
		String apiStatus = normalizeApiStatus(body.getAuditState());
		WalletKycStateEnums localState = WalletKycStateEnums.fromApiStatus(apiStatus);
		String failedReason = WalletKycStateEnums.ERROR_APPROVE.equals(localState) ? body.getAuditRemark() : null;
		walletUserService.syncKycFromWebhook(body.getUid(), apiStatus, failedReason);
		WalletUserEntity user = walletUserDao.findByWalletUid(body.getUid());
		if (user != null) {
			String bizId = "wallet:kyc:" + body.getUid() + ":" + apiStatus
					+ (StringUtils.isEmpty(body.getEventId()) ? "" : (":" + body.getEventId()));
			if (WalletKycStateEnums.SUCCESS_APPROVE.equals(localState)) {
				walletNotifyService.notify(user, WalletNotifyEventEnums.KYC_PASS, bizId,
						WalletNotifyConstants.JUMP_KYC, null);
			} else if (WalletKycStateEnums.ERROR_APPROVE.equals(localState)) {
				String reason = StringUtils.isEmpty(failedReason) ? "-" : failedReason;
				walletNotifyService.notify(user, WalletNotifyEventEnums.KYC_REJECT, bizId,
						WalletNotifyConstants.JUMP_KYC, null, reason);
			}
		}
		log.info("wallet webhook kyc synced walletUid={} status={}", body.getUid(), apiStatus);
	}

	/** 充值结果：更新流水/余额，并联动提现打款 */
	@Transactional(rollbackFor = Exception.class)
	public void handleCardRechargeResult(WalletWebhookNotifyRequest body) {
		if (body.getUserBankcardId() == null) {
			throw new BaseException("recharge webhook userBankcardId empty");
		}
		BigDecimal rechargeAmt = parseAmount(body.getRechargeAmount());
		if (rechargeAmt == null || rechargeAmt.compareTo(BigDecimal.ZERO) <= 0) {
			log.info("wallet webhook recharge skip zero amount eventId={}", body.getEventId());
			return;
		}
		WalletBankcardEntity card = walletBankcardDao.findByUserBankcardId(body.getUserBankcardId());
		if (card == null) {
			log.warn("wallet webhook recharge card not found userBankcardId={}", body.getUserBankcardId());
			return;
		}
		WalletCardTransactionEntity txn = findRechargeTransaction(body);
		if (txn != null) {
			walletCardTransactionDao.updateOrderState(txn.getId(),
					WalletLogStatusEnums.POSTED.getIntCode(), WalletLogStatusEnums.POSTED.getLabel(),
					body.getOrderId());
			markRechargeWalletLogPosted(txn, card);
			if (!StringUtils.isEmpty(body.getCardNo())) {
				walletBankcardDao.updateCardNo(card.getId(), body.getCardNo());
			} else {
				walletBankcardSyncSupport.syncCardNo(card);
			}
			syncCardBalance(card);
			linkWithdrawPayout(txn, body.getOrderId(), true, null);
			WalletUserEntity user = walletUserDao.findByWalletUid(card.getWalletUid());
			String amtText = rechargeAmt.toPlainString();
			String bizId = "wallet:recharge:ok:"
					+ (StringUtils.isEmpty(txn.getRequestOrderId()) ? body.getEventId() : txn.getRequestOrderId());
			walletNotifyService.notify(user, WalletNotifyEventEnums.CARD_RECHARGE_SUCCESS, bizId,
					WalletNotifyConstants.JUMP_CARD, String.valueOf(card.getId()), amtText);
			return;
		}
		// 无本地流水时仍尝试同步余额
		if (!StringUtils.isEmpty(body.getCardNo())) {
			walletBankcardDao.updateCardNo(card.getId(), body.getCardNo());
		} else {
			walletBankcardSyncSupport.syncCardNo(card);
		}
		syncCardBalance(card);
		log.info("wallet webhook recharge no local txn userBankcardId={} orderId={}",
				body.getUserBankcardId(), body.getOrderId());
	}

	/**
	 * 银行卡状态变更（对齐 onetoken cardStateUp + Apifox cardStatusChange）。
	 * status：cardActive / cardFreeze / cardClose
	 */
	private void handleCardStatusChange(WalletWebhookNotifyRequest body) {
		if (StringUtils.isEmpty(body.getStatus())) {
			throw new BaseException("card status webhook status empty");
		}
		WalletBankcardEntity card = resolveWebhookCard(body);
		if (card == null) {
			log.warn("wallet webhook card status card not found userBankcardId={} oldUserBankcardId={}",
					body.getUserBankcardId(), body.getOldUserBankcardId());
			return;
		}
		WalletCardStatusEnums status = WalletCardStatusEnums.fromWebhookStatus(body.getStatus());
		if (status == null) {
			throw new BaseException("card status webhook unknown status=" + body.getStatus());
		}
		if (WalletCardStatusEnums.ACTIVE.equals(status)) {
			handleCardActiveWebhook(body, card);
			return;
		}
		if (WalletCardStatusEnums.FREEZE.equals(status)) {
			handleCardFreezeWebhook(body, card);
			return;
		}
		if (WalletCardStatusEnums.CLOSED.equals(status)) {
			WalletUserEntity user = walletUserDao.findByWalletUid(card.getWalletUid());
			walletCardCloseWebhookSupport.handleCardClose(body, card, user);
			return;
		}
		persistWebhookCardNo(body, card);
		walletBankcardDao.updateCardStatus(card.getId(), status.getCode(), status.getLabel());
		log.info("wallet webhook card status updated userBankcardId={} status={}",
				card.getUserBankcardId(), status.getLabel());
	}

	/**
	 * cardActive：轮询确认三方已 ACTIVE 后再落本地成功；仍激活中则保持本地激活中并失败回执以便三方重试
	 *（对齐 onetoken WebhookServiceImpl.activeCard）。
	 */
	private void handleCardActiveWebhook(WalletWebhookNotifyRequest body, WalletBankcardEntity card) {
		boolean alreadyActive = card.getCardStatus() != null
				&& card.getCardStatus() == WalletCardStatusEnums.ACTIVE.getCode();
		if (alreadyActive) {
			persistWebhookCardNo(body, card);
			log.info("wallet webhook card active idempotent userBankcardId={}", body.getUserBankcardId());
			return;
		}
		boolean wasFrozen = card.getCardStatus() != null
				&& card.getCardStatus() == WalletCardStatusEnums.FREEZE.getCode();
		if (!confirmThirdPartyCardActive(card)) {
			// 本地维持激活中，不核销冻结；抛错让三方重推，待真正 ACTIVE 再成功
			ensureLocalActivating(card);
			throw new BaseException("card not active after retry userBankcardId=" + body.getUserBankcardId());
		}
		persistWebhookCardNo(body, card);
		walletBankcardDao.updateCardStatus(card.getId(),
				WalletCardStatusEnums.ACTIVE.getCode(), WalletCardStatusEnums.ACTIVE.getLabel());
		WalletUserEntity user = walletUserDao.findByWalletUid(card.getWalletUid());
		if (user != null) {
			walletUserService.markAccountActivated(user.getId());
		}
		walletOpenCardSettlementService.onCardActivated(card);
		String bizId = "wallet:card:active:" + card.getUserBankcardId()
				+ (StringUtils.isEmpty(body.getEventId()) ? "" : (":" + body.getEventId()));
		WalletNotifyEventEnums event = wasFrozen
				? WalletNotifyEventEnums.CARD_UNFREEZE : WalletNotifyEventEnums.CARD_OPEN_SUCCESS;
		walletNotifyService.notify(user, event, bizId,
				WalletNotifyConstants.JUMP_CARD, String.valueOf(card.getId()));
		log.info("wallet webhook card activated userBankcardId={} cardNoPresent={} wasFrozen={}",
				body.getUserBankcardId(), !StringUtils.isEmpty(card.getCardNo()), wasFrozen);
	}

	/** 回调确认失败时确保本地为激活中（避免误标成功） */
	private void ensureLocalActivating(WalletBankcardEntity card) {
		if (card == null || card.getId() == null) {
			return;
		}
		boolean activating = card.getCardStatus() != null
				&& card.getCardStatus() == WalletCardStatusEnums.ACTIVATING.getCode();
		if (activating) {
			return;
		}
		walletBankcardDao.updateCardStatus(card.getId(),
				WalletCardStatusEnums.ACTIVATING.getCode(), WalletCardStatusEnums.ACTIVATING.getLabel());
		card.setCardStatus(WalletCardStatusEnums.ACTIVATING.getCode());
		card.setCardStatusName(WalletCardStatusEnums.ACTIVATING.getLabel());
		if (card.getCardApplyId() != null) {
			WalletCardApplyEntity apply = walletCardApplyDao.selectById(card.getCardApplyId());
			if (apply != null
					&& !Integer.valueOf(WalletCardApplyStateEnums.SUCCESS_ACTIVATION.getCode())
					.equals(apply.getApplyState())
					&& !Integer.valueOf(WalletCardApplyStateEnums.ERROR_ACTIVATION.getCode())
					.equals(apply.getApplyState())) {
				walletCardApplyDao.updateApplyState(apply.getId(),
						WalletCardApplyStateEnums.PROCESS_ACTIVATION.getCode(),
						WalletCardApplyStateEnums.PROCESS_ACTIVATION.getLabel(), null);
			}
		}
	}

	/** cardFreeze：更新本地冻结状态 */
	private void handleCardFreezeWebhook(WalletWebhookNotifyRequest body, WalletBankcardEntity card) {
		persistWebhookCardNo(body, card);
		walletBankcardDao.updateCardStatus(card.getId(),
				WalletCardStatusEnums.FREEZE.getCode(), WalletCardStatusEnums.FREEZE.getLabel());
		WalletUserEntity user = walletUserDao.findByWalletUid(card.getWalletUid());
		String reasonSuffix = StringUtils.isEmpty(body.getReason()) ? "" : ("：" + body.getReason());
		walletNotifyService.notify(user, WalletNotifyEventEnums.CARD_FREEZE,
				"wallet:card:freeze:" + card.getUserBankcardId()
						+ (StringUtils.isEmpty(body.getEventId()) ? "" : (":" + body.getEventId())),
				WalletNotifyConstants.JUMP_CARD, String.valueOf(card.getId()), reasonSuffix);
		log.info("wallet webhook card frozen userBankcardId={} reason={}",
				card.getUserBankcardId(), body.getReason());
	}

	private void persistWebhookCardNo(WalletWebhookNotifyRequest body, WalletBankcardEntity card) {
		if (!StringUtils.isEmpty(body.getCardNo())) {
			walletBankcardDao.updateCardNo(card.getId(), body.getCardNo());
			card.setCardNo(body.getCardNo());
			return;
		}
		walletBankcardSyncSupport.syncCardNo(card);
	}

	/** 按 userBankcardId / oldUserBankcardId 解析本地卡 */
	private WalletBankcardEntity resolveWebhookCard(WalletWebhookNotifyRequest body) {
		if (body.getUserBankcardId() != null) {
			WalletBankcardEntity card = walletBankcardDao.findByUserBankcardId(body.getUserBankcardId());
			if (card != null) {
				return card;
			}
		}
		Long oldId = parseOldUserBankcardId(body.getOldUserBankcardId());
		if (oldId == null) {
			return null;
		}
		return walletBankcardDao.findByUserBankcardId(oldId);
	}

	private static Long parseOldUserBankcardId(String oldUserBankcardId) {
		if (StringUtils.isEmpty(oldUserBankcardId)) {
			return null;
		}
		try {
			return Long.parseLong(oldUserBankcardId.trim());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 轮询三方 /api/bankcard/info 确认卡已激活（对齐 onetoken activeCard 重试逻辑）。
	 */
	private boolean confirmThirdPartyCardActive(WalletBankcardEntity card) {
		if (card == null || card.getWalletUid() == null || card.getUserBankcardId() == null) {
			return false;
		}
		int maxRetries = WalletWebhookConstants.CARD_ACTIVE_CONFIRM_MAX_RETRIES;
		long intervalMs = WalletWebhookConstants.CARD_ACTIVE_CONFIRM_RETRY_INTERVAL_MS;
		ThirdBankcardInfoResp info = queryThirdPartyCardInfo(card);
		int retryCount = 0;
		while (!isThirdPartyCardActive(info) && retryCount < maxRetries) {
			retryCount++;
			log.warn("wallet webhook card not active yet userBankcardId={} status={} retry={}/{}",
					card.getUserBankcardId(), info == null ? null : info.getStatus(), retryCount, maxRetries);
			try {
				Thread.sleep(intervalMs);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.error("wallet webhook card active confirm interrupted userBankcardId={}",
						card.getUserBankcardId(), e);
				return false;
			}
			info = queryThirdPartyCardInfo(card);
		}
		if (!isThirdPartyCardActive(info)) {
			log.error("wallet webhook card active confirm failed userBankcardId={} finalStatus={}",
					card.getUserBankcardId(), info == null ? null : info.getStatus());
			return false;
		}
		walletBankcardSyncSupport.syncCardNo(card);
		return true;
	}

	private ThirdBankcardInfoResp queryThirdPartyCardInfo(WalletBankcardEntity card) {
		try {
			return thirdService.getBankcardInfo(card.getWalletUid(), card.getUserBankcardId());
		} catch (Exception e) {
			log.error("wallet webhook query card info failed userBankcardId={}", card.getUserBankcardId(), e);
			return null;
		}
	}

	private static boolean isThirdPartyCardActive(ThirdBankcardInfoResp info) {
		return info != null
				&& WalletCardStatusEnums.ACTIVE.equals(WalletCardStatusEnums.fromThirdPartyCode(info.getStatus()));
	}

	/** 卡交易通知：落 wallet_card_transaction */
	@Transactional(rollbackFor = Exception.class)
	public void handleTransactionCreated(WalletWebhookNotifyRequest body) {
		WalletWebhookNotifyRequest.TransactionPayload tx = body.getTransaction();
		if (body.getUserBankcardId() == null || tx == null || StringUtils.isEmpty(tx.getTransactionId())) {
			throw new BaseException("transaction webhook param empty");
		}
		WalletCardTransactionEntity existed = walletCardTransactionDao.findByThirdOrderNum(tx.getTransactionId());
		if (existed != null) {
			log.info("wallet webhook transaction duplicate transactionId={}", tx.getTransactionId());
			return;
		}
		WalletBankcardEntity card = walletBankcardDao.findByUserBankcardId(body.getUserBankcardId());
		if (card == null) {
			log.warn("wallet webhook transaction card not found userBankcardId={}", body.getUserBankcardId());
			return;
		}
		Date now = new Date();
		WalletCardTransactionEntity row = new WalletCardTransactionEntity();
		row.setWalletUserId(card.getWalletUserId());
		row.setWalletUid(card.getWalletUid());
		row.setWalletBankcardId(card.getId());
		row.setUserBankcardId(card.getUserBankcardId());
		row.setCardProductId(card.getCardProductId());
		row.setCardUuid(card.getCardUuid());
		row.setCardNo(StringUtils.isEmpty(body.getCardNo()) ? card.getCardNo() : body.getCardNo());
		row.setThirdOrderNum(tx.getTransactionId());
		row.setBizType(mapTransBizType(tx.getTransType()));
		row.setTransType(mapTransTypeName(tx.getTransType()));
		row.setOrderState(mapTransOrderState(tx.getTransStatus()));
		row.setOrderStateName(mapTransOrderStateName(tx.getTransStatus()));
		row.setLocalCurrency(tx.getLocalCurrency());
		row.setLocalCurrencyAmt(formatSignedAmount(tx.getTransType(), tx.getLocalCurrencyAmt()));
		row.setTransCurrency(tx.getLocalCurrency());
		row.setTransCurrencyAmt(row.getLocalCurrencyAmt());
		row.setHandlingFees(parseAmount(tx.getFeeAmount()));
		row.setTitle(buildTransactionTitle(card.getCardNo(), tx.getMerchantName()));
		if (body.getCreateAt() != null && body.getCreateAt() > 0) {
			row.setSetTime(new Date(body.getCreateAt()));
		} else {
			row.setSetTime(now);
		}
		row.setGmtModified(now);
		walletCardTransactionDao.insert(row);

		if (Integer.valueOf(WalletLogStatusEnums.POSTED.getIntCode()).equals(row.getOrderState())) {
			syncCardBalance(card);
		}
		WalletUserEntity user = walletUserDao.findByWalletUid(card.getWalletUid());
		String titleText = row.getTitle() == null ? tx.getTransactionId() : row.getTitle();
		walletNotifyService.notify(user, WalletNotifyEventEnums.CARD_TXN,
				"wallet:txn:" + tx.getTransactionId(), WalletNotifyConstants.JUMP_CARD,
				String.valueOf(card.getId()), titleText);
		log.info("wallet webhook transaction inserted transactionId={} userBankcardId={}",
				tx.getTransactionId(), body.getUserBankcardId());
	}

	/** 3DS 授权：系统消息 + 极光提醒 */
	private void handle3ds(WalletWebhookNotifyRequest body) {
		log.info("wallet webhook 3ds eventId={} userBankcardId={} authId={} hasOtp={}",
				body.getEventId(), body.getUserBankcardId(), body.getAuthId(),
				!StringUtils.isEmpty(body.getOtp()));
		WalletBankcardEntity card = body.getUserBankcardId() == null
				? null : walletBankcardDao.findByUserBankcardId(body.getUserBankcardId());
		if (card == null) {
			return;
		}
		WalletUserEntity user = walletUserDao.findByWalletUid(card.getWalletUid());
		String bizId = "wallet:3ds:"
				+ (StringUtils.isEmpty(body.getAuthId()) ? body.getEventId() : body.getAuthId());
		walletNotifyService.notify(user, WalletNotifyEventEnums.CARD_3DS, bizId,
				WalletNotifyConstants.JUMP_CARD, String.valueOf(card.getId()));
	}

	/** 商户充值：审计日志即可 */
	private void handleMerchantRecharge(WalletWebhookNotifyRequest body) {
		log.info("wallet webhook merchantRecharge eventId={} amount={} txHash={}",
				body.getEventId(), body.getAmount(), body.getTxHash());
	}

	/** 充值成功：账变由处理中 → 已入账 */
	private void markRechargeWalletLogPosted(WalletCardTransactionEntity txn, WalletBankcardEntity card) {
		if (txn == null || StringUtils.isEmpty(txn.getRequestOrderId())) {
			return;
		}
		WalletLogEntity walletLog = walletLogDao.findByOutOrderNo(txn.getRequestOrderId());
		if (walletLog == null) {
			return;
		}
		walletLog.setStatus(WalletLogStatusEnums.POSTED.getCode());
		if (card != null) {
			if (!StringUtils.isEmpty(card.getCardNo())) {
				walletLog.setToName(card.getCardNo());
				walletLog.setToAccount(card.getCardNo());
			}
			walletLog.setWalletBankcardId(card.getId());
		}
		walletLog.setGmtModified(new Date());
		try {
			walletLogDao.updateById(walletLog);
		} catch (Exception e) {
			log.error("wallet webhook recharge wallet log update failed requestOrderId={}",
					txn.getRequestOrderId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private WalletCardTransactionEntity findRechargeTransaction(WalletWebhookNotifyRequest body) {
		if (!StringUtils.isEmpty(body.getOrderId())) {
			WalletCardTransactionEntity byThird = walletCardTransactionDao.findByThirdOrderNum(body.getOrderId());
			if (byThird != null) {
				return byThird;
			}
			WalletCardTransactionEntity byRequest = walletCardTransactionDao.findByRequestOrderId(body.getOrderId());
			if (byRequest != null) {
				return byRequest;
			}
		}
		if (body.getUserBankcardId() == null) {
			return null;
		}
		return walletCardTransactionDao.findLatestRechargeByUserBankcardId(body.getUserBankcardId());
	}

	/** 充值成功时若关联 WALLET 提现单，确认打款 */
	private void linkWithdrawPayout(WalletCardTransactionEntity txn, String thirdOrderNo,
			boolean success, String failReason) {
		if (txn == null || StringUtils.isEmpty(txn.getRequestOrderId())) {
			return;
		}
		UserWithdrawOrderEntity order = userWithdrawOrderDao.findByRequestOrderId(txn.getRequestOrderId());
		if (order == null) {
			return;
		}
		if (!WalletWebhookConstants.GATEWAY_WALLET.equalsIgnoreCase(order.getGateway())) {
			return;
		}
		withdrawPayoutService.handleCallback(order.getOrderNo(), success, thirdOrderNo, failReason);
		log.info("wallet webhook withdraw payout linked orderNo={} success={}", order.getOrderNo(), success);
	}

	/** 拉三方余额并回写本地卡缓存 */
	private void syncCardBalance(WalletBankcardEntity card) {
		if (card == null || card.getWalletUid() == null || card.getUserBankcardId() == null) {
			return;
		}
		try {
			ThirdBankcardBalanceResp balanceResp = thirdService.getBankcardBalance(
					card.getWalletUid(), card.getUserBankcardId());
			BigDecimal balance = parseAmount(balanceResp == null ? null : balanceResp.getBalance());
			if (balance != null) {
				walletBankcardDao.updateBalance(card.getId(), balance);
			}
		} catch (Exception e) {
			log.error("wallet webhook sync balance failed userBankcardId={}", card.getUserBankcardId(), e);
		}
	}

	private boolean verifyInboundSign(WalletWebhookNotifyRequest body, HttpServletRequest request) {
		if (!thirdPartyProperties.isWebhookSignVerifyEnabled()) {
			return true;
		}
		String publicKey = thirdPartyProperties.getPublicKey();
		if (StringUtils.isEmpty(publicKey)) {
			log.error("wallet webhook sign verify enabled but publicKey empty");
			return false;
		}
		String appId = headerOrConfig(request, HEADER_APP_ID, thirdPartyProperties.getAppId());
		String nonce = request.getHeader(HEADER_NONCE);
		String timestamp = request.getHeader(HEADER_TIMESTAMP);
		String sign = request.getHeader(HEADER_SIGN);
		try {
			return RsaVerifyUtil.verifySign(appId, nonce, timestamp, body, sign, publicKey);
		} catch (Exception e) {
			log.error("wallet webhook sign verify error eventId={}", body.getEventId(), e);
			return false;
		}
	}

	private static String headerOrConfig(HttpServletRequest request, String header, String fallback) {
		String value = request.getHeader(header);
		if (StringUtils.isEmpty(value)) {
			return fallback;
		}
		return value;
	}

	private WalletWebhookEventEntity insertEvent(WalletWebhookNotifyRequest body) {
		Date now = new Date();
		WalletWebhookEventEntity row = new WalletWebhookEventEntity();
		row.setEventId(body.getEventId());
		row.setEventType(body.getEventType());
		row.setWalletUid(body.getUid());
		row.setUserBankcardId(body.getUserBankcardId());
		row.setBizNo(resolveBizNo(body));
		row.setPayload(toPayloadJson(body));
		row.setProcessStatus(WalletWebhookConstants.PROCESS_PENDING);
		row.setRetryCount(0);
		row.setSetTime(now);
		row.setGmtModified(now);
		try {
			walletWebhookEventDao.insert(row);
			return row;
		} catch (DuplicateKeyException e) {
			WalletWebhookEventEntity existed = walletWebhookEventDao.findByEventId(body.getEventId());
			if (existed != null) {
				return existed;
			}
			throw new BaseException("webhook event insert failed", e);
		} catch (Exception e) {
			throw new BaseException("webhook event insert failed", e);
		}
	}

	private void markProcessResult(Long id, int status, String msg) {
		if (id == null) {
			return;
		}
		try {
			walletWebhookEventDao.updateProcessResult(id, status, msg);
		} catch (Exception e) {
			log.error("wallet webhook update process result failed id={}", id, e);
		}
	}

	private String toPayloadJson(WalletWebhookNotifyRequest body) {
		try {
			return objectMapper.writeValueAsString(body);
		} catch (Exception e) {
			log.error("wallet webhook serialize payload failed eventId={}", body.getEventId(), e);
			return "{}";
		}
	}

	private static String resolveBizNo(WalletWebhookNotifyRequest body) {
		if (!StringUtils.isEmpty(body.getOrderId())) {
			return body.getOrderId();
		}
		if (body.getTransaction() != null && !StringUtils.isEmpty(body.getTransaction().getTransactionId())) {
			return body.getTransaction().getTransactionId();
		}
		return null;
	}

	private static String normalizeApiStatus(String auditState) {
		if (StringUtils.isEmpty(auditState)) {
			return auditState;
		}
		return auditState.trim().toLowerCase();
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

	private static BigDecimal formatSignedAmount(Integer transType, String amount) {
		BigDecimal value = parseAmount(amount);
		if (value == null) {
			return null;
		}
		// 1=消费扣款，2=退款入账（对齐 onetoken TransTypeEnums）
		if (Integer.valueOf(1).equals(transType)) {
			return value.abs().negate();
		}
		if (Integer.valueOf(2).equals(transType)) {
			return value.abs();
		}
		return value;
	}

	private static String mapTransBizType(Integer transType) {
		if (Integer.valueOf(1).equals(transType)) {
			return WalletConstants.BIZ_AUTH;
		}
		if (Integer.valueOf(2).equals(transType)) {
			return WalletConstants.BIZ_REFUND;
		}
		return WalletConstants.BIZ_AUTH;
	}

	private static String mapTransTypeName(Integer transType) {
		if (Integer.valueOf(1).equals(transType)) {
			return WalletConstants.BIZ_AUTH;
		}
		if (Integer.valueOf(2).equals(transType)) {
			return WalletConstants.BIZ_REFUND;
		}
		return WalletConstants.BIZ_AUTH;
	}

	/** 三方 transStatus：1 成功，2 失败，其它处理中 */
	private static Integer mapTransOrderState(Integer transStatus) {
		if (Integer.valueOf(1).equals(transStatus)) {
			return WalletLogStatusEnums.POSTED.getIntCode();
		}
		if (Integer.valueOf(2).equals(transStatus)) {
			return WalletLogStatusEnums.FAILED.getIntCode();
		}
		return WalletLogStatusEnums.PROCESSING.getIntCode();
	}

	private static String mapTransOrderStateName(Integer transStatus) {
		if (Integer.valueOf(1).equals(transStatus)) {
			return WalletLogStatusEnums.POSTED.getLabel();
		}
		if (Integer.valueOf(2).equals(transStatus)) {
			return WalletLogStatusEnums.FAILED.getLabel();
		}
		return WalletLogStatusEnums.PROCESSING.getLabel();
	}

	private static String buildTransactionTitle(String cardNo, String merchantName) {
		String suffix = StringUtils.isEmpty(cardNo) ? "****" : maskCardTail(cardNo);
		if (StringUtils.isEmpty(merchantName)) {
			return "Card:" + suffix;
		}
		return "Card:" + suffix + " " + merchantName;
	}

	private static String maskCardTail(String cardNo) {
		if (cardNo.length() <= 4) {
			return cardNo;
		}
		return cardNo.substring(cardNo.length() - 4);
	}

	private static String truncateMsg(String msg) {
		if (msg == null) {
			return null;
		}
		if (msg.length() <= 500) {
			return msg;
		}
		return msg.substring(0, 500);
	}
}
