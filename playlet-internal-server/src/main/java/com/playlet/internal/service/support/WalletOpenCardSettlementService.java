package com.playlet.internal.service.support;

import com.playlet.internal.api.request.BankcardRechargeRequest;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.dao.wallet.WalletAccountDao;
import com.playlet.internal.dao.wallet.WalletCardApplyDao;
import com.playlet.internal.dao.wallet.WalletCardTransactionDao;
import com.playlet.internal.dao.wallet.WalletLogDao;
import com.playlet.internal.dao.wallet.WalletUserDao;
import com.playlet.internal.entity.wallet.WalletAccountEntity;
import com.playlet.internal.entity.wallet.WalletBankcardEntity;
import com.playlet.internal.entity.wallet.WalletCardApplyEntity;
import com.playlet.internal.entity.wallet.WalletCardTransactionEntity;
import com.playlet.internal.entity.wallet.WalletLogEntity;
import com.playlet.internal.entity.wallet.WalletUserEntity;
import com.playlet.internal.enums.WalletCardApplyStateEnums;
import com.playlet.internal.enums.WalletKycStateEnums;
import com.playlet.internal.enums.WalletLogOperateTypeEnums;
import com.playlet.internal.enums.WalletLogStatusEnums;
import com.playlet.internal.enums.WalletLogTradeTypeEnums;
import com.playlet.internal.enums.WalletLogisticsStateEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.third.ThirdService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.OrderCodeFactory;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.WalletRequestOrderIdSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 开卡资金结算：冻结核销、首充、KYC 失败解冻、虚拟卡激活终态。
 */
@Slf4j
@Service
public class WalletOpenCardSettlementService {

	private static final String ADDRESS_PLACEHOLDER = "-";

	@Autowired
	private WalletAccountDao walletAccountDao;
	@Autowired
	private WalletCardApplyDao walletCardApplyDao;
	@Autowired
	private WalletCardTransactionDao walletCardTransactionDao;
	@Autowired
	private WalletLogDao walletLogDao;
	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private ThirdService thirdService;

	/**
	 * 开卡成功后的预存首充（不重复扣 available_balance，费用已在申请时冻结）。
	 */
	public void firstTopUpAfterOpenCard(WalletUserEntity user, WalletCardApplyEntity apply,
			WalletBankcardEntity card) {
		if (user == null || apply == null || card == null || card.getUserBankcardId() == null) {
			return;
		}
		BigDecimal preSave = apply.getPreSaveCost() == null ? BigDecimal.ZERO : apply.getPreSaveCost();
		if (preSave.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		int amount = preSave.intValue();
		if (amount <= 0) {
			return;
		}
		String requestOrderId = buildFirstTopUpOrderId(apply);
		WalletCardTransactionEntity existed = walletCardTransactionDao.findByRequestOrderId(requestOrderId);
		if (existed != null) {
			log.info("wallet first topup idempotent applyId={} requestOrderId={}", apply.getId(), requestOrderId);
			return;
		}
		// 对齐 onetoken：开卡后稍候再首充
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("wallet first topup sleep interrupted applyId={}", apply.getId());
		}
		BankcardRechargeRequest req = new BankcardRechargeRequest();
		req.setUserBankcardId(card.getUserBankcardId());
		req.setAmount(amount);
		req.setRequestOrderId(requestOrderId);
		try {
			thirdService.rechargeBankcard(user.getWalletUid(), req);
		} catch (BaseException e) {
			log.error("wallet first topup third failed applyId={} walletUid={} requestOrderId={}",
					apply.getId(), user.getWalletUid(), requestOrderId, e);
			throw e;
		} catch (Exception e) {
			log.error("wallet first topup third error applyId={} walletUid={} requestOrderId={}",
					apply.getId(), user.getWalletUid(), requestOrderId, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		insertFirstTopUpTransaction(user, card, req);
		upsertFirstTopUpWalletLog(user, apply, card, req, amount);
		log.info("wallet first topup submitted applyId={} walletUid={} userBankcardId={} amount={}",
				apply.getId(), user.getWalletUid(), card.getUserBankcardId(), amount);
	}

	/** 虚拟卡开卡成功后：首充 */
	public void afterVirtualCardIssued(WalletUserEntity user, WalletCardApplyEntity apply,
			WalletBankcardEntity card) {
		firstTopUpAfterOpenCard(user, apply, card);
	}

	/**
	 * 核销开卡冻结（发货 / 虚拟卡激活）：仅钱包充值方式扣减 open_freeze_balance。
	 */
	public void settleApplyFreeze(WalletCardApplyEntity apply) {
		if (apply == null || !isWalletTopup(apply)) {
			return;
		}
		BigDecimal total = apply.getOpenCardTotal() == null ? BigDecimal.ZERO : apply.getOpenCardTotal();
		if (total.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(apply.getWalletUserId());
		if (account == null) {
			log.warn("wallet settle open freeze account missing applyId={} walletUserId={}",
					apply.getId(), apply.getWalletUserId());
			return;
		}
		try {
			int rows = walletAccountDao.settleOpenCardFreeze(account.getId(), total);
			if (rows <= 0) {
				log.warn("wallet settle open freeze skipped applyId={} accountId={} amount={}",
						apply.getId(), account.getId(), total);
				return;
			}
		} catch (Exception e) {
			log.error("wallet settle open freeze failed applyId={} accountId={}", apply.getId(), account.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("wallet settle open freeze success applyId={} walletUserId={} amount={}",
				apply.getId(), apply.getWalletUserId(), total);
	}

	/** 拒绝 / KYC 失败：解冻开卡冻结金额回可用余额，并记「取消开卡」账变 */
	public void unfreezeApplyTotal(WalletCardApplyEntity apply) {
		if (apply == null || !isWalletTopup(apply)) {
			return;
		}
		BigDecimal total = apply.getOpenCardTotal() == null ? BigDecimal.ZERO : apply.getOpenCardTotal();
		if (total.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		String thawOutOrderNo = buildThawOutOrderNo(apply.getId());
		if (walletLogDao.findByOutOrderNo(thawOutOrderNo) != null) {
			log.info("wallet unfreeze open card thaw log exists applyId={}", apply.getId());
			return;
		}
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(apply.getWalletUserId());
		if (account == null) {
			log.warn("wallet unfreeze open card account missing applyId={} walletUserId={}",
					apply.getId(), apply.getWalletUserId());
			return;
		}
		try {
			int rows = walletAccountDao.unfreezeOpenCardBalance(account.getId(), total);
			if (rows <= 0) {
				log.warn("wallet unfreeze open card skipped applyId={} accountId={} amount={}",
						apply.getId(), account.getId(), total);
				return;
			}
		} catch (Exception e) {
			log.error("wallet unfreeze open card failed applyId={} accountId={}", apply.getId(), account.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		WalletUserEntity user = walletUserDao.selectById(apply.getWalletUserId());
		if (user == null) {
			log.warn("wallet unfreeze open card user missing applyId={} walletUserId={}",
					apply.getId(), apply.getWalletUserId());
			return;
		}
		WalletAccountEntity accountAfter = walletAccountDao.findByWalletUserId(apply.getWalletUserId());
		insertOpenCardThawWalletLog(user, accountAfter, total, thawOutOrderNo);
		log.info("wallet unfreeze open card success applyId={} walletUserId={} amount={}",
				apply.getId(), apply.getWalletUserId(), total);
	}

	/** 卡激活：虚拟卡核销冻结；实体/虚拟均标记申请单激活成功 */
	public void onCardActivated(WalletBankcardEntity card) {
		if (card == null || card.getCardApplyId() == null) {
			return;
		}
		boolean virtual = WalletConstants.BANKCARD_NATURE_VIRTUAL.equalsIgnoreCase(card.getBankcardNature())
				|| WalletConstants.BANKCARD_NATURE_VIRTUAL.equalsIgnoreCase(card.getCardType());
		WalletCardApplyEntity apply = walletCardApplyDao.selectById(card.getCardApplyId());
		if (apply == null) {
			return;
		}
		if (Integer.valueOf(WalletCardApplyStateEnums.SUCCESS_ACTIVATION.getCode()).equals(apply.getApplyState())) {
			return;
		}
		if (virtual) {
			settleApplyFreeze(apply);
		}
		markApplySuccessActivation(apply.getId());
	}

	/** KYC 通过：实体卡申请单进入待发货 */
	public void markPhysicalWaitShippingOnKycSuccess(Long walletUserId) {
		if (walletUserId == null) {
			return;
		}
		try {
			walletCardApplyDao.updateKycAndShippingByWalletUserId(walletUserId,
					WalletKycStateEnums.SUCCESS_APPROVE.getCode(),
					WalletKycStateEnums.SUCCESS_APPROVE.getLabel(),
					WalletLogisticsStateEnums.WAIT_SUCCESS.getCode(),
					WalletLogisticsStateEnums.WAIT_SUCCESS.getLabel());
		} catch (Exception e) {
			log.error("wallet kyc success update shipping failed walletUserId={}", walletUserId, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/** KYC 失败：认证中的申请单标记失败并解冻 */
	public void failKycPendingApplies(Long walletUserId, String failedReason) {
		if (walletUserId == null) {
			return;
		}
		List<WalletCardApplyEntity> pendingList = walletCardApplyDao.findByWalletUserIdAndKycState(
				walletUserId, WalletKycStateEnums.PROCESS_APPROVE.getCode());
		if (pendingList == null || pendingList.isEmpty()) {
			return;
		}
		for (WalletCardApplyEntity apply : pendingList) {
			if (apply.getApplyState() == null
					|| apply.getApplyState() != WalletCardApplyStateEnums.WAIT_ACTIVATION.getCode()) {
				continue;
			}
			failSingleApplyOnKyc(apply, failedReason);
		}
	}

	/** 定时任务：单条申请 KYC 轮询失败处理 */
	public void failSingleApplyOnKyc(WalletCardApplyEntity apply, String failedReason) {
		if (apply == null) {
			return;
		}
		String reason = StringUtils.isEmpty(failedReason) ? null : failedReason.trim();
		try {
			walletCardApplyDao.updateKycSnapshot(apply.getId(),
					WalletKycStateEnums.ERROR_APPROVE.getCode(),
					WalletKycStateEnums.ERROR_APPROVE.getLabel(), reason);
			walletCardApplyDao.updateApplyState(apply.getId(),
					WalletCardApplyStateEnums.ERROR_ACTIVATION.getCode(),
					WalletCardApplyStateEnums.ERROR_ACTIVATION.getLabel(), reason);
		} catch (Exception e) {
			log.error("wallet kyc fail update apply failed applyId={}", apply.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		unfreezeApplyTotal(apply);
		log.info("wallet kyc fail apply applyId={} walletUserId={}", apply.getId(), apply.getWalletUserId());
	}

	private void markApplySuccessActivation(Long applyId) {
		try {
			walletCardApplyDao.updateApplyState(applyId,
					WalletCardApplyStateEnums.SUCCESS_ACTIVATION.getCode(),
					WalletCardApplyStateEnums.SUCCESS_ACTIVATION.getLabel(), null);
		} catch (Exception e) {
			log.error("wallet mark apply success activation failed applyId={}", applyId, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("wallet apply success activation applyId={}", applyId);
	}

	private static boolean isWalletTopup(WalletCardApplyEntity apply) {
		return Integer.valueOf(WalletConstants.TOPUP_TYPE_WALLET).equals(apply.getTopupType());
	}

	private static String buildThawOutOrderNo(Long applyId) {
		return applyId + WalletConstants.WALLET_LOG_OUT_ORDER_THAW_SUFFIX;
	}

	/** 开卡失败解冻账变（对齐 onetoken addWalletOpenFreeze / OPEN_CARD_THAW） */
	private void insertOpenCardThawWalletLog(WalletUserEntity user, WalletAccountEntity account,
			BigDecimal amount, String thawOutOrderNo) {
		if (user == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		BigDecimal balanceAfter = account == null || account.getAvailableBalance() == null
				? BigDecimal.ZERO : account.getAvailableBalance();
		Date now = new Date();
		WalletLogEntity logEntity = new WalletLogEntity();
		logEntity.setOrderNo(OrderCodeFactory.getOrderCode(user.getWalletUid()));
		logEntity.setOutOrderNo(thawOutOrderNo);
		logEntity.setWalletUserId(user.getId());
		logEntity.setWalletUid(user.getWalletUid());
		logEntity.setTradeType(WalletLogTradeTypeEnums.INCOME.getCode());
		logEntity.setTitle(I18nUtil.getMessage("wallet.log.open_card_thaw"));
		logEntity.setPrimevalMoney(balanceAfter);
		logEntity.setPrimevalMoneyUnit(WalletConstants.DEFAULT_CURRENCY);
		logEntity.setRealMoney(amount);
		logEntity.setServiceCharge(BigDecimal.ZERO);
		logEntity.setFormName(user.getEmail());
		logEntity.setFormAccount(String.valueOf(user.getWalletUid()));
		logEntity.setToName(user.getEmail());
		logEntity.setToAccount(user.getEmail());
		logEntity.setStatus(WalletLogStatusEnums.POSTED.getCode());
		logEntity.setOperateType(WalletLogOperateTypeEnums.OPEN_CARD_THAW.getCode());
		logEntity.setSetTime(now);
		logEntity.setGmtModified(now);
		try {
			walletLogDao.insert(logEntity);
		} catch (DuplicateKeyException e) {
			log.warn("wallet open card thaw wallet log duplicate outOrderNo={}", thawOutOrderNo, e);
		} catch (Exception e) {
			log.error("wallet open card thaw wallet log failed walletUserId={} outOrderNo={}",
					user.getId(), thawOutOrderNo, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private static String buildFirstTopUpOrderId(WalletCardApplyEntity apply) {
		if (!StringUtils.isEmpty(apply.getRequestOrderId())) {
			return apply.getRequestOrderId() + "-FT";
		}
		return WalletRequestOrderIdSupport.resolve(null, WalletConstants.REQUEST_ORDER_PREFIX_FIRST_TOPUP,
				apply.getWalletUid());
	}

	private void insertFirstTopUpTransaction(WalletUserEntity user, WalletBankcardEntity card,
			BankcardRechargeRequest query) {
		Date now = new Date();
		BigDecimal amount = BigDecimal.valueOf(query.getAmount());
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
		txn.setBizType(WalletConstants.BIZ_RECHARGE);
		txn.setTransType(WalletConstants.TRANS_TOPUP);
		txn.setOrderState(WalletLogStatusEnums.PROCESSING.getIntCode());
		txn.setOrderStateName(WalletLogStatusEnums.PROCESSING.getLabel());
		txn.setLocalCurrency(currency);
		txn.setLocalCurrencyAmt(amount);
		txn.setTransCurrency(currency);
		txn.setTransCurrencyAmt(amount);
		txn.setTitle("开卡首充");
		txn.setSetTime(now);
		txn.setGmtModified(now);
		try {
			walletCardTransactionDao.insert(txn);
		} catch (DuplicateKeyException e) {
			log.warn("wallet first topup txn duplicate requestOrderId={}", query.getRequestOrderId(), e);
		} catch (Exception e) {
			log.error("wallet first topup txn insert failed requestOrderId={}", query.getRequestOrderId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/**
	 * 开卡首充账变：优先更新申请时「开卡冻结」记录为「开卡」，否则新增（对齐 onetoken firstTopUp）。
	 */
	private void upsertFirstTopUpWalletLog(WalletUserEntity user, WalletCardApplyEntity apply,
			WalletBankcardEntity card, BankcardRechargeRequest query, int amount) {
		if (user == null || apply == null || query == null) {
			return;
		}
		Date now = new Date();
		BigDecimal realMoney = BigDecimal.valueOf(amount);
		WalletLogEntity existed = walletLogDao.findByOutOrderNo(String.valueOf(apply.getId()));
		if (existed != null) {
			existed.setOrderNo(OrderCodeFactory.getOrderCode(user.getWalletUid()));
			existed.setOutOrderNo(query.getRequestOrderId());
			existed.setOperateType(WalletLogOperateTypeEnums.OPEN_CARD.getCode());
			existed.setTitle(I18nUtil.getMessage("wallet.log.open_card"));
			existed.setStatus(WalletLogStatusEnums.PROCESSING.getCode());
			existed.setRealMoney(realMoney);
			existed.setServiceCharge(BigDecimal.ZERO);
			if (card != null) {
				existed.setWalletBankcardId(card.getId());
				existed.setToName(card.getCardNo());
				existed.setToAccount(card.getCardNo());
			}
			existed.setGmtModified(now);
			try {
				walletLogDao.updateById(existed);
			} catch (Exception e) {
				log.error("wallet first topup wallet log update failed applyId={} requestOrderId={}",
						apply.getId(), query.getRequestOrderId(), e);
				throw new BaseException(I18nUtil.getMessage("base_error"), e);
			}
			return;
		}
		WalletLogEntity logEntity = new WalletLogEntity();
		logEntity.setOrderNo(OrderCodeFactory.getOrderCode(user.getWalletUid()));
		logEntity.setOutOrderNo(query.getRequestOrderId());
		logEntity.setWalletUserId(user.getId());
		logEntity.setWalletUid(user.getWalletUid());
		logEntity.setTradeType(WalletLogTradeTypeEnums.EXPENDITURE.getCode());
		logEntity.setTitle(I18nUtil.getMessage("wallet.log.open_card"));
		logEntity.setPrimevalMoneyUnit(WalletConstants.DEFAULT_CURRENCY);
		logEntity.setRealMoney(realMoney);
		logEntity.setServiceCharge(BigDecimal.ZERO);
		logEntity.setFormName(user.getEmail());
		logEntity.setFormAccount(String.valueOf(user.getWalletUid()));
		if (card != null) {
			logEntity.setWalletBankcardId(card.getId());
			logEntity.setToName(card.getCardNo());
			logEntity.setToAccount(card.getCardNo());
		}
		logEntity.setStatus(WalletLogStatusEnums.PROCESSING.getCode());
		logEntity.setOperateType(WalletLogOperateTypeEnums.OPEN_CARD.getCode());
		logEntity.setSetTime(now);
		logEntity.setGmtModified(now);
		try {
			walletLogDao.insert(logEntity);
		} catch (Exception e) {
			log.error("wallet first topup wallet log insert failed applyId={} requestOrderId={}",
					apply.getId(), query.getRequestOrderId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/** 仅 deliveryAddressId 时落最小邮寄快照（占位字段满足非空约束） */
	public static String addressPlaceholder() {
		return ADDRESS_PLACEHOLDER;
	}
}
