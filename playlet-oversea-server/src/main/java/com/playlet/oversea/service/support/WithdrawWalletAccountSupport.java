package com.playlet.oversea.service.support;

import com.playlet.oversea.constants.WalletConstants;
import com.playlet.oversea.dao.wallet.WalletAccountDao;
import com.playlet.oversea.dao.wallet.WalletCardTransactionDao;
import com.playlet.oversea.dao.wallet.WalletLogDao;
import com.playlet.oversea.dao.wallet.WalletUserDao;
import com.playlet.oversea.entity.wallet.WalletAccountEntity;
import com.playlet.oversea.entity.wallet.WalletCardTransactionEntity;
import com.playlet.oversea.entity.wallet.WalletLogEntity;
import com.playlet.oversea.entity.wallet.WalletUserEntity;
import com.playlet.oversea.enums.WalletKycStateEnums;
import com.playlet.oversea.enums.WalletLogOperateTypeEnums;
import com.playlet.oversea.enums.WalletLogStatusEnums;
import com.playlet.oversea.enums.WalletLogTradeTypeEnums;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 提现入账：校验钱包账户、增加 U 余额并写入 wallet_log / wallet_card_transaction。
 */
@Slf4j
@Component
public class WithdrawWalletAccountSupport {

	private static final String COIN_WITHDRAW_FORM = "COIN";

	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletAccountDao walletAccountDao;
	@Autowired
	private WalletLogDao walletLogDao;
	@Autowired
	private WalletCardTransactionDao walletCardTransactionDao;

	/** 是否已开通钱包账户（wallet_user + wallet_account） */
	public boolean isReady(Integer userType, Integer localUid) {
		return requireAccount(userType, localUid, false) != null;
	}

	/**
	 * 金币提现入账：增加 available_balance 并写入 wallet_log（幂等 outOrderNo=orderNo）。
	 *
	 * @param actualAmt 实到 U（已扣 withdraw_config 手续费）
	 * @param feeAmt    提现手续费（展示在 service_charge）
	 */
	public BigDecimal creditCoinWithdraw(Integer userType, Integer localUid, BigDecimal actualAmt,
			BigDecimal feeAmt, int points, String orderNo, Long withdrawOrderId) {
		if (actualAmt == null || actualAmt.signum() <= 0) {
			throw new BaseException(I18nUtil.getMessage("withdraw.actual_zero"));
		}
		WalletUserEntity user = requireWalletUser(userType, localUid);
		WalletAccountEntity account = requireAccount(user, true);
		BigDecimal balanceBefore = nvl(account.getAvailableBalance());
		int rows = walletAccountDao.addAvailableBalance(account.getId(), actualAmt);
		if (rows <= 0) {
			log.error("withdraw credit balance failed walletAccountId={} orderNo={} amount={}",
					account.getId(), orderNo, actualAmt);
			throw new BaseException(I18nUtil.getMessage("base_error"));
		}
		BigDecimal balanceAfter = balanceBefore.add(actualAmt);
		insertCoinWithdrawWalletLog(user, balanceBefore, actualAmt, feeAmt, points, orderNo);
		insertCoinWithdrawCardTransaction(user, actualAmt, feeAmt, points, orderNo, withdrawOrderId);
		log.info("withdraw credited walletAccountId={} orderNo={} amount={} balanceAfter={}",
				account.getId(), orderNo, actualAmt, balanceAfter);
		return balanceAfter;
	}

	/** 写入金币提现账变；outOrderNo 幂等 */
	private void insertCoinWithdrawWalletLog(WalletUserEntity user, BigDecimal balanceBefore,
			BigDecimal actualAmt, BigDecimal feeAmt, int points, String orderNo) {
		if (walletLogDao.findByOutOrderNo(orderNo) != null) {
			return;
		}
		Date now = new Date();
		BigDecimal serviceCharge = nvl(feeAmt);
		WalletLogEntity logEntity = new WalletLogEntity();
		logEntity.setOrderNo(orderNo);
		logEntity.setOutOrderNo(orderNo);
		logEntity.setWalletUserId(user.getId());
		logEntity.setWalletUid(user.getWalletUid());
		logEntity.setTradeType(WalletLogTradeTypeEnums.INCOME.getCode());
		logEntity.setTitle(I18nUtil.getMessage("wallet.log.coin_to_wallet"));
		logEntity.setPrimevalMoney(balanceBefore);
		logEntity.setPrimevalMoneyUnit(WalletConstants.DEFAULT_CURRENCY);
		logEntity.setRealMoney(actualAmt);
		logEntity.setServiceCharge(serviceCharge);
		logEntity.setFormName(COIN_WITHDRAW_FORM);
		logEntity.setFormAccount(String.valueOf(points));
		logEntity.setToName(user.getEmail());
		logEntity.setToAccount(user.getEmail());
		logEntity.setMemo("points=" + points);
		logEntity.setStatus(WalletLogStatusEnums.POSTED.getCode());
		logEntity.setOperateType(WalletLogOperateTypeEnums.COIN_TO_WALLET.getCode());
		logEntity.setSetTime(now);
		logEntity.setGmtModified(now);
		try {
			walletLogDao.insert(logEntity);
		} catch (DuplicateKeyException e) {
			log.warn("coin withdraw wallet log duplicate orderNo={}", orderNo, e);
		} catch (Exception e) {
			log.error("coin withdraw wallet log failed walletUserId={} orderNo={}", user.getId(), orderNo, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/** 金币提现：同步落 wallet_card_transaction，供 /transaction/list 与后台流水查询 */
	private void insertCoinWithdrawCardTransaction(WalletUserEntity user, BigDecimal actualAmt,
			BigDecimal feeAmt, int points, String orderNo, Long withdrawOrderId) {
		if (walletCardTransactionDao.findByRequestOrderId(orderNo) != null) {
			return;
		}
		Date now = new Date();
		BigDecimal serviceCharge = nvl(feeAmt);
		WalletCardTransactionEntity txn = new WalletCardTransactionEntity();
		txn.setWalletUserId(user.getId());
		txn.setWalletUid(user.getWalletUid());
		txn.setWalletBankcardId(null);
		txn.setUserBankcardId(WalletConstants.WALLET_BALANCE_BANKCARD_PLACEHOLDER);
		txn.setRequestOrderId(orderNo);
		txn.setWithdrawOrderId(withdrawOrderId);
		txn.setBizType(WalletConstants.BIZ_COIN_TO_WALLET);
		txn.setTransType(WalletConstants.TRANS_COIN_TO_WALLET);
		txn.setPayType(WalletConstants.TOPUP_TYPE_WALLET);
		txn.setOrderState(WalletLogStatusEnums.POSTED.getIntCode());
		txn.setOrderStateName(WalletLogStatusEnums.POSTED.getLabel());
		txn.setLocalCurrency(WalletConstants.DEFAULT_CURRENCY);
		txn.setLocalCurrencyAmt(actualAmt);
		txn.setTransCurrency(WalletConstants.DEFAULT_CURRENCY);
		txn.setTransCurrencyAmt(actualAmt);
		txn.setHandlingFees(serviceCharge);
		txn.setTitle(I18nUtil.getMessage("wallet.log.coin_to_wallet"));
		txn.setSetTime(now);
		txn.setGmtModified(now);
		try {
			walletCardTransactionDao.insert(txn);
		} catch (DuplicateKeyException e) {
			log.warn("coin withdraw card txn duplicate orderNo={}", orderNo, e);
		} catch (Exception e) {
			log.error("coin withdraw card txn failed walletUserId={} orderNo={} points={}",
					user.getId(), orderNo, points, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private WalletUserEntity requireWalletUser(Integer userType, Integer localUid) {
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			throw new BaseException(I18nUtil.getMessage("wallet.not_opened"));
		}
		return user;
	}

	private WalletAccountEntity requireAccount(Integer userType, Integer localUid, boolean createIfMissing) {
		if (userType == null || localUid == null) {
			return null;
		}
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return null;
		}
		return requireAccount(user, createIfMissing);
	}

	private WalletAccountEntity requireAccount(WalletUserEntity user, boolean createIfMissing) {
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
		if (account != null) {
			return account;
		}
		if (!createIfMissing) {
			return null;
		}
		return insertWalletAccount(user);
	}

	private WalletAccountEntity insertWalletAccount(WalletUserEntity user) {
		Date now = new Date();
		WalletAccountEntity account = new WalletAccountEntity();
		account.setWalletUserId(user.getId());
		account.setWalletUid(user.getWalletUid());
		account.setKycState(WalletKycStateEnums.WAIT_APPROVE.getCode());
		account.setKycStateName(WalletKycStateEnums.WAIT_APPROVE.getLabel());
		account.setActivationState(0);
		account.setAvailableBalance(BigDecimal.ZERO);
		account.setFreezeBalance(BigDecimal.ZERO);
		account.setOpenFreezeBalance(BigDecimal.ZERO);
		account.setCurrency(WalletConstants.DEFAULT_CURRENCY);
		account.setSetTime(now);
		account.setGmtModified(now);
		try {
			walletAccountDao.insert(account);
			return account;
		} catch (DuplicateKeyException e) {
			log.warn("withdraw wallet account duplicate walletUserId={}", user.getId(), e);
			return walletAccountDao.findByWalletUserId(user.getId());
		} catch (Exception e) {
			log.error("withdraw wallet account insert failed walletUserId={}", user.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private static BigDecimal nvl(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}
}
