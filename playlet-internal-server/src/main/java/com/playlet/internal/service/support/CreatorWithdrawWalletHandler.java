package com.playlet.internal.service.support;

import com.playlet.internal.dao.creator.CreatorAccountDao;
import com.playlet.internal.dao.creator.CreatorCoinLedgerDao;
import com.playlet.internal.dao.creator.CreatorProfileDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.creator.CreatorCoinLedgerEntity;
import com.playlet.internal.entity.creator.CreatorProfileEntity;
import com.playlet.internal.enums.CoinBizTypeEnums;
import com.playlet.internal.enums.WithdrawUserTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.GenericityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * 作家提现：扣 creator_account，记 creator_coin_ledger，OnePay 在 creator_profile。
 */
@Slf4j
@Component
public class CreatorWithdrawWalletHandler implements WithdrawWalletHandler {

	@Autowired
	private CreatorAccountDao creatorAccountDao;
	@Autowired
	private CreatorProfileDao creatorProfileDao;
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
		CreatorProfileEntity profile = creatorProfileDao.findByCreatorId(uid);
		if (profile != null) {
			snap.setOnepayBindStatus(profile.getOnepayBindStatus());
			snap.setOnepayAccount(profile.getOnepayAccount());
		}
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
	public String findOpenId(Integer uid) {
		CreatorProfileEntity profile = creatorProfileDao.findByCreatorId(uid);
		return profile == null ? null : profile.getOnepayOpenId();
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
		long before = account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		long frozen = account == null || account.getFrozenCoinBalance() == null ? 0L : account.getFrozenCoinBalance();
		CreatorCoinLedgerEntity ledger = new CreatorCoinLedgerEntity();
		ledger.setCreatorId(uid);
		ledger.setChangeAmt((long) -amt);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before - amt);
		ledger.setFrozenBefore(frozen);
		ledger.setFrozenAfter(Math.max(0L, frozen - amt));
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setRemark("提现扣减");
		try {
			GenericityUtil.setDate(ledger);
			creatorCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			log.warn("creator withdraw ledger duplicate creatorId={} bizId={}", uid, bizId);
		} catch (Exception e) {
			log.error("creator withdraw ledger insert failed creatorId={} bizId={}", uid, bizId, e);
			throw new BaseException("creator withdraw ledger insert failed", e);
		}
	}

	private static long nvl(Long v) {
		return v == null ? 0L : v;
	}
}
