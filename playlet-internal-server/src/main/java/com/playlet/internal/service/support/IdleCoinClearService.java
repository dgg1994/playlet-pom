package com.playlet.internal.service.support;

import com.playlet.internal.constants.CoinConstants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.coin.UserCoinClearLogDao;
import com.playlet.internal.dao.welfare.UserCoinLedgerDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.coin.CoinGlobalConfigEntity;
import com.playlet.internal.entity.coin.UserCoinClearLogEntity;
import com.playlet.internal.entity.welfare.UserCoinLedgerEntity;
import com.playlet.internal.utils.GenericityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 久未登录清币：按 coin_global_config 扫描并清零可用余额。
 */
@Slf4j
@Service
public class IdleCoinClearService {

	@Autowired
	private CoinRateSupport coinRateSupport;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private UserCoinClearLogDao userCoinClearLogDao;
	@Autowired
	private UserCoinLedgerDao userCoinLedgerDao;

	/**
	 * @return 本批清币用户数
	 */
	@Transactional(rollbackFor = Exception.class)
	public int clearIdleUsers() {
		CoinGlobalConfigEntity cfg = coinRateSupport.requireConfig();
		if (cfg == null || !Integer.valueOf(CoinConstants.IDLE_CLEAR_ON).equals(cfg.getIdleClearEnabled())) {
			return 0;
		}
		int idleDays = cfg.getIdleClearDays() == null || cfg.getIdleClearDays() < 1
				? 30 : cfg.getIdleClearDays();
		List<AppAccountEntity> list = appAccountDao.findIdleWithCoin(idleDays, CoinConstants.IDLE_CLEAR_BATCH);
		if (list == null || list.isEmpty()) {
			return 0;
		}
		int cleared = 0;
		for (AppAccountEntity account : list) {
			if (account == null || account.getId() == null) {
				continue;
			}
			try {
				if (clearOne(account, idleDays)) {
					cleared++;
				}
			} catch (Exception e) {
				log.error("idle coin clear failed uid={}", account.getId(), e);
			}
		}
		log.info("idle coin clear batch done idleDays={} cleared={}", idleDays, cleared);
		return cleared;
	}

	private boolean clearOne(AppAccountEntity account, int idleDays) throws Exception {
		long balance = account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		long frozen = account.getFrozenCoinBalance() == null ? 0L : account.getFrozenCoinBalance();
		long available = balance - frozen;
		if (available <= 0) {
			return false;
		}
		Integer uid = account.getId();
		String bizId = "IDLE_CLEAR_" + uid + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt((int) -Math.min(available, Integer.MAX_VALUE));
		ledger.setBalanceBefore(balance);
		ledger.setBalanceAfter(frozen);
		ledger.setFrozenBefore(frozen);
		ledger.setFrozenAfter(frozen);
		ledger.setBizType(CoinConstants.BIZ_IDLE_CLEAR);
		ledger.setBizId(bizId);
		ledger.setTaskCode(CoinConstants.TASK_IDLE_CLEAR);
		ledger.setAdBoostFlag(0);
		ledger.setRemark("久未登录清币");
		GenericityUtil.setDate(ledger);
		try {
			userCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			return false;
		}
		int rows = appAccountDao.clearAvailableCoin(uid);
		if (rows <= 0) {
			return false;
		}
		UserCoinClearLogEntity clearLog = new UserCoinClearLogEntity();
		clearLog.setUid(uid);
		clearLog.setClearCoin(available);
		clearLog.setIdleDays(idleDays);
		clearLog.setLastLogin(account.getLastLoginTime());
		clearLog.setSetTime(new Date());
		userCoinClearLogDao.insert(clearLog);
		log.info("idle coin cleared uid={} clearCoin={} idleDays={}", uid, available, idleDays);
		return true;
	}
}
