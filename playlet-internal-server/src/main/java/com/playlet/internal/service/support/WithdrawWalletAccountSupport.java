package com.playlet.internal.service.support;

import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.dao.wallet.WalletAccountDao;
import com.playlet.internal.dao.wallet.WalletUserDao;
import com.playlet.internal.entity.wallet.WalletAccountEntity;
import com.playlet.internal.entity.wallet.WalletUserEntity;
import com.playlet.internal.enums.WalletKycStateEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 提现入账：校验钱包账户并向 available_balance 增加 U。
 */
@Slf4j
@Component
public class WithdrawWalletAccountSupport {

	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletAccountDao walletAccountDao;

	/** 是否已开通钱包账户（wallet_user + wallet_account） */
	public boolean isReady(Integer userType, Integer localUid) {
		return requireAccount(userType, localUid, false) != null;
	}

	/** 提现入账：原子增加可用余额，返回入账后余额 */
	public BigDecimal creditAvailableBalance(Integer userType, Integer localUid, BigDecimal amount, String orderNo) {
		if (amount == null || amount.signum() <= 0) {
			throw new BaseException(I18nUtil.getMessage("withdraw.actual_zero"));
		}
		WalletAccountEntity account = requireAccount(userType, localUid, true);
		int rows = walletAccountDao.addAvailableBalance(account.getId(), amount);
		if (rows <= 0) {
			log.error("withdraw credit balance failed walletAccountId={} orderNo={} amount={}",
					account.getId(), orderNo, amount);
			throw new BaseException(I18nUtil.getMessage("base_error"));
		}
		BigDecimal after = nvl(account.getAvailableBalance()).add(amount);
		log.info("withdraw credited walletAccountId={} orderNo={} amount={} balanceAfter={}",
				account.getId(), orderNo, amount, after);
		return after;
	}

	private WalletAccountEntity requireAccount(Integer userType, Integer localUid, boolean createIfMissing) {
		if (userType == null || localUid == null) {
			return null;
		}
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return null;
		}
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
