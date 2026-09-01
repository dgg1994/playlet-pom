package com.playlet.internal.service.support;

import com.playlet.internal.api.request.BankcardRechargeRequest;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.dao.wallet.WalletAccountDao;
import com.playlet.internal.dao.wallet.WalletCardApplyDao;
import com.playlet.internal.dao.wallet.WalletCardTransactionDao;
import com.playlet.internal.entity.wallet.WalletAccountEntity;
import com.playlet.internal.entity.wallet.WalletBankcardEntity;
import com.playlet.internal.entity.wallet.WalletCardApplyEntity;
import com.playlet.internal.entity.wallet.WalletCardTransactionEntity;
import com.playlet.internal.entity.wallet.WalletUserEntity;
import com.playlet.internal.enums.WalletCardApplyStateEnums;
import com.playlet.internal.enums.WalletKycStateEnums;
import com.playlet.internal.enums.WalletLogisticsStateEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.third.ThirdService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.OrderCodeFactory;
import com.playlet.internal.utils.StringUtils;
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

	/** 拒绝 / KYC 失败：解冻开卡冻结金额回可用余额 */
	public void unfreezeApplyTotal(WalletCardApplyEntity apply) {
		if (apply == null || !isWalletTopup(apply)) {
			return;
		}
		BigDecimal total = apply.getOpenCardTotal() == null ? BigDecimal.ZERO : apply.getOpenCardTotal();
		if (total.compareTo(BigDecimal.ZERO) <= 0) {
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
			}
		} catch (Exception e) {
			log.error("wallet unfreeze open card failed applyId={} accountId={}", apply.getId(), account.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
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

	private static String buildFirstTopUpOrderId(WalletCardApplyEntity apply) {
		if (!StringUtils.isEmpty(apply.getRequestOrderId())) {
			return apply.getRequestOrderId() + "-FT";
		}
		return "FT" + OrderCodeFactory.getOrderCode(apply.getWalletUid());
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
		txn.setOrderState(WalletConstants.ORDER_STATE_PENDING);
		txn.setOrderStateName(WalletConstants.ORDER_STATE_PENDING_NAME);
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

	/** 仅 deliveryAddressId 时落最小邮寄快照（占位字段满足非空约束） */
	public static String addressPlaceholder() {
		return ADDRESS_PLACEHOLDER;
	}
}
