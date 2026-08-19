package com.playlet.internal.service.support;

import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.welfare.UserCoinLedgerDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.welfare.UserCoinLedgerEntity;
import com.playlet.internal.enums.CoinBizTypeEnums;
import com.playlet.internal.enums.WithdrawUserTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.GenericityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * C 端提现：扣 app_account，记 user_coin_ledger。
 */
@Slf4j
@Component
public class AppWithdrawWalletHandler implements WithdrawWalletHandler {

	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private UserCoinLedgerDao userCoinLedgerDao;

	@Override
	public WithdrawUserTypeEnums userType() {
		return WithdrawUserTypeEnums.APP;
	}

	@Override
	public WithdrawWalletSnapshot load(Integer uid) {
		AppAccountEntity account = appAccountDao.findByUid(uid);
		WithdrawWalletSnapshot snap = new WithdrawWalletSnapshot();
		if (account == null) {
			return snap;
		}
		snap.setCoinBalance(nvl(account.getCoinBalance()));
		snap.setFrozenCoinBalance(nvl(account.getFrozenCoinBalance()));
		snap.setOnepayBindStatus(account.getOnepayBindStatus());
		snap.setOnepayAccount(account.getOnepayAccount());
		return snap;
	}

	@Override
	public int freeze(Integer uid, int amt) {
		return appAccountDao.freezeCoinBalance(uid, amt);
	}

	@Override
	public int settleFrozen(Integer uid, int amt) {
		return appAccountDao.settleFrozenCoin(uid, amt);
	}

	@Override
	public int unfreeze(Integer uid, int amt) {
		return appAccountDao.unfreezeCoinBalance(uid, amt);
	}

	@Override
	public String findOpenId(Integer uid) {
		AppAccountEntity account = appAccountDao.findByUid(uid);
		return account == null ? null : account.getOnepayOpenId();
	}

	@Override
	public void writeWithdrawLedger(Integer uid, int amt, String orderNo) {
		if (uid == null || amt <= 0) {
			return;
		}
		String bizType = CoinBizTypeEnums.WITHDRAW.getName();
		String bizId = "WITHDRAW:" + orderNo;
		UserCoinLedgerEntity exist = userCoinLedgerDao.findByBiz(uid, bizType, bizId);
		if (exist != null) {
			return;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		long before = account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt(-amt);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before - amt);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setTaskCode("");
		ledger.setAdBoostFlag(0);
		ledger.setRemark("提现扣减");
		try {
			GenericityUtil.setDate(ledger);
			userCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			log.warn("withdraw ledger duplicate uid={} bizId={}", uid, bizId);
		} catch (Exception e) {
			log.error("withdraw ledger insert failed uid={} bizId={}", uid, bizId, e);
			throw new BaseException("withdraw ledger insert failed", e);
		}
	}

	private static long nvl(Long v) {
		return v == null ? 0L : v;
	}
}
