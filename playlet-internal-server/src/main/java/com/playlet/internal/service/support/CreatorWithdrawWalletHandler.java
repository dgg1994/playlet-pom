package com.playlet.internal.service.support;

import com.playlet.internal.dao.creator.CreatorAccountDao;
import com.playlet.internal.dao.creator.CreatorCoinLedgerDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.creator.CreatorCoinLedgerEntity;
import com.playlet.internal.enums.CoinBizTypeEnums;
import com.playlet.internal.enums.WithdrawUserTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.GenericityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * 作家提现：扣 creator_account，记 creator_coin_ledger。
 */
@Slf4j
@Component
public class CreatorWithdrawWalletHandler implements WithdrawWalletHandler {

	@Autowired
	private CreatorAccountDao creatorAccountDao;
	@Autowired
	private CreatorCoinLedgerDao creatorCoinLedgerDao;

	@Override
	public WithdrawUserTypeEnums userType() {
		return WithdrawUserTypeEnums.CREATOR;
	}

	@Override
	public WithdrawWalletSnapshot load(Integer uid) {
		CreatorAccountEntity account = creatorAccountDao.selectById(uid);
		WithdrawWalletSnapshot snap = new WithdrawWalletSnapshot();
		if (account == null) {
			return snap;
		}
		snap.setCoinBalance(nvl(account.getCoinBalance()));
		snap.setFrozenCoinBalance(nvl(account.getFrozenCoinBalance()));
		return snap;
	}

	@Override
	public int freeze(Integer uid, int amt) {
		return creatorAccountDao.freezeCoinBalance(uid, amt);
	}

	@Override
	public int settleFrozen(Integer uid, int amt) {
		return creatorAccountDao.settleFrozenCoin(uid, amt);
	}

	@Override
	public int unfreeze(Integer uid, int amt) {
		return creatorAccountDao.unfreezeCoinBalance(uid, amt);
	}

	@Override
	public void writeWithdrawFreezeLedger(Integer uid, int amt, String orderNo) {
		if (uid == null || amt <= 0) {
			return;
		}
		String bizType = CoinBizTypeEnums.WITHDRAW_FREEZE.getName();
		String bizId = "WITHDRAW:" + orderNo;
		if (creatorCoinLedgerDao.findByBiz(uid, bizType, bizId) != null) {
			return;
		}
		CreatorAccountEntity account = creatorAccountDao.selectById(uid);
		long before = nvl(account == null ? null : account.getCoinBalance());
		long frozenAfter = nvl(account == null ? null : account.getFrozenCoinBalance());
		long frozenBefore = Math.max(0L, frozenAfter - amt);
		CreatorCoinLedgerEntity ledger = new CreatorCoinLedgerEntity();
		ledger.setCreatorId(uid);
		ledger.setChangeAmt(0L);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before);
		ledger.setFrozenBefore(frozenBefore);
		ledger.setFrozenAfter(frozenAfter);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setRemark("提现冻结");
		insertLedger(ledger, uid, bizId, "creator withdraw freeze ledger");
	}

	@Override
	public void writeWithdrawLedger(Integer uid, int amt, String orderNo) {
		if (uid == null || amt <= 0) {
			return;
		}
		String bizType = CoinBizTypeEnums.WITHDRAW.getName();
		String bizId = "WITHDRAW:" + orderNo;
		if (creatorCoinLedgerDao.findByBiz(uid, bizType, bizId) != null) {
			return;
		}
		CreatorAccountEntity account = creatorAccountDao.selectById(uid);
		long after = account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		long frozenAfter = account == null || account.getFrozenCoinBalance() == null ? 0L : account.getFrozenCoinBalance();
		CreatorCoinLedgerEntity ledger = new CreatorCoinLedgerEntity();
		ledger.setCreatorId(uid);
		ledger.setChangeAmt((long) -amt);
		ledger.setBalanceAfter(after);
		ledger.setBalanceBefore(after + amt);
		ledger.setFrozenBefore(frozenAfter);
		ledger.setFrozenAfter(frozenAfter);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setRemark("提现扣减");
		insertLedger(ledger, uid, bizId, "creator withdraw ledger");
	}

	@Override
	public void writeWithdrawRefundLedger(Integer uid, int amt, String orderNo) {
		if (uid == null || amt <= 0) {
			return;
		}
		String bizType = CoinBizTypeEnums.WITHDRAW_REFUND.getName();
		String bizId = "WITHDRAW:" + orderNo;
		if (creatorCoinLedgerDao.findByBiz(uid, bizType, bizId) != null) {
			return;
		}
		CreatorAccountEntity account = creatorAccountDao.selectById(uid);
		long before = nvl(account == null ? null : account.getCoinBalance());
		long frozenAfter = nvl(account == null ? null : account.getFrozenCoinBalance());
		CreatorCoinLedgerEntity ledger = new CreatorCoinLedgerEntity();
		ledger.setCreatorId(uid);
		ledger.setChangeAmt(0L);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before);
		ledger.setFrozenBefore(frozenAfter + amt);
		ledger.setFrozenAfter(frozenAfter);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setRemark("提现退回");
		insertLedger(ledger, uid, bizId, "creator withdraw refund ledger");
	}

	private void insertLedger(CreatorCoinLedgerEntity ledger, Integer uid, String bizId, String scene) {
		try {
			GenericityUtil.setDate(ledger);
			creatorCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			log.warn("{} duplicate creatorId={} bizId={}", scene, uid, bizId);
		} catch (Exception e) {
			log.error("{} insert failed creatorId={} bizId={}", scene, uid, bizId, e);
			throw new BaseException(scene + " insert failed", e);
		}
	}

	private static long nvl(Long v) {
		return v == null ? 0L : v;
	}
}
