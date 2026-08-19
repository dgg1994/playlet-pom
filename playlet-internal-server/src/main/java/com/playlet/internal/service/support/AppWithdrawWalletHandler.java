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
	public void writeWithdrawFreezeLedger(Integer uid, int amt, String orderNo) {
		if (uid == null || amt <= 0) {
			return;
		}
		String bizType = CoinBizTypeEnums.WITHDRAW_FREEZE.getName();
		String bizId = "WITHDRAW:" + orderNo;
		if (userCoinLedgerDao.findByBiz(uid, bizType, bizId) != null) {
			return;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		long before = nvl(account == null ? null : account.getCoinBalance());
		long frozenBefore = nvl(account == null ? null : account.getFrozenCoinBalance()) - amt;
		if (frozenBefore < 0) {
			frozenBefore = 0;
		}
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt(0);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before);
		ledger.setFrozenBefore(frozenBefore);
		ledger.setFrozenAfter(frozenBefore + amt);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setTaskCode("");
		ledger.setAdBoostFlag(0);
		ledger.setRemark("提现冻结");
		insertLedger(ledger, uid, bizId, "withdraw freeze ledger");
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
		long before = nvl(account == null ? null : account.getCoinBalance());
		long frozen = nvl(account == null ? null : account.getFrozenCoinBalance());
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt(-amt);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before - amt);
		ledger.setFrozenBefore(frozen);
		ledger.setFrozenAfter(Math.max(0L, frozen - amt));
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setTaskCode("");
		ledger.setAdBoostFlag(0);
		ledger.setRemark("提现扣减");
		insertLedger(ledger, uid, bizId, "withdraw ledger");
	}

	@Override
	public void writeWithdrawRefundLedger(Integer uid, int amt, String orderNo) {
		if (uid == null || amt <= 0) {
			return;
		}
		String bizType = CoinBizTypeEnums.WITHDRAW_REFUND.getName();
		String bizId = "WITHDRAW:" + orderNo;
		if (userCoinLedgerDao.findByBiz(uid, bizType, bizId) != null) {
			return;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		long before = nvl(account == null ? null : account.getCoinBalance());
		long frozen = nvl(account == null ? null : account.getFrozenCoinBalance());
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt(0);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before);
		ledger.setFrozenBefore(frozen + amt);
		ledger.setFrozenAfter(frozen);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setTaskCode("");
		ledger.setAdBoostFlag(0);
		ledger.setRemark("提现退回");
		insertLedger(ledger, uid, bizId, "withdraw refund ledger");
	}

	private void insertLedger(UserCoinLedgerEntity ledger, Integer uid, String bizId, String scene) {
		try {
			GenericityUtil.setDate(ledger);
			userCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			log.warn("{} duplicate uid={} bizId={}", scene, uid, bizId);
		} catch (Exception e) {
			log.error("{} insert failed uid={} bizId={}", scene, uid, bizId, e);
			throw new BaseException(scene + " insert failed", e);
		}
	}

	private static long nvl(Long v) {
		return v == null ? 0L : v;
	}
}
