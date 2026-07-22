package com.playlet.internal.service.impl;

import com.playlet.internal.api.response.WatchGiftHomeSummaryEntity;
import com.playlet.internal.api.response.WatchGiftOpResult;
import com.playlet.internal.api.response.WatchGiftRewardItemEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.welfare.UserCoinLedgerDao;
import com.playlet.internal.dao.welfare.UserWatchGiftProgressDao;
import com.playlet.internal.dao.welfare.WatchGiftGlobalConfigDao;
import com.playlet.internal.dao.welfare.WatchGiftRewardConfigDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.welfare.UserCoinLedgerEntity;
import com.playlet.internal.entity.welfare.UserWatchGiftProgressEntity;
import com.playlet.internal.entity.welfare.WatchGiftGlobalConfigEntity;
import com.playlet.internal.entity.welfare.WatchGiftRewardConfigEntity;
import com.playlet.internal.enums.CoinBizTypeEnums;
import com.playlet.internal.enums.WatchGiftRewardStateEnums;
import com.playlet.internal.service.WatchGiftService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 观影礼：按时长分档；时长由 theater view/report 的 deltaSeconds 累计。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WatchGiftServiceImpl extends BaseApiService implements WatchGiftService {

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final String BIZ_ID_PREFIX = "WATCH_GIFT:";

	@Autowired
	private WatchGiftGlobalConfigDao watchGiftGlobalConfigDao;
	@Autowired
	private WatchGiftRewardConfigDao watchGiftRewardConfigDao;
	@Autowired
	private UserWatchGiftProgressDao userWatchGiftProgressDao;
	@Autowired
	private UserCoinLedgerDao userCoinLedgerDao;
	@Autowired
	private AppAccountDao appAccountDao;

	@Override
	public ResponseBase claim(@RequestParam Integer gearIndex, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		WatchGiftOpResult result = doClaim(uid, gearIndex);
		if (!result.isOk()) {
			return setResultError(I18nUtil.getMessage(result.getMsgKey()));
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase claimAll(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		WatchGiftOpResult result = doClaimAll(uid);
		if (!result.isOk()) {
			return setResultError(I18nUtil.getMessage(result.getMsgKey()));
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public WatchGiftHomeSummaryEntity buildHomeSummary(Integer uid) {
		if (uid == null) {
			return null;
		}
		WatchGiftGlobalConfigEntity config = watchGiftGlobalConfigDao.findEnabledOne();
		List<WatchGiftRewardConfigEntity> rewards = watchGiftRewardConfigDao.findEnabledList();
		if (config == null || rewards == null || rewards.isEmpty()) {
			return null;
		}
		return buildSummaryInternal(uid, config, rewards);
	}

	@Override
	public void addWatchSeconds(Integer uid, int deltaSeconds, String extInfo) {
		if (uid == null || deltaSeconds <= 0) {
			return;
		}
		try {
			WatchGiftGlobalConfigEntity config = watchGiftGlobalConfigDao.findEnabledOne();
			if (config == null) {
				return;
			}
			int maxDelta = config.getMaxDeltaSecPerReport() == null ? 120 : Math.max(1, config.getMaxDeltaSecPerReport());
			int minInterval = config.getMinReportIntervalSec() == null ? 0 : Math.max(0, config.getMinReportIntervalSec());
			int maxDaily = config.getMaxDailySeconds() == null ? 7200 : Math.max(1, config.getMaxDailySeconds());
			int delta = Math.min(deltaSeconds, maxDelta);

			ZoneId zone = resolveZone(config.getTimezone());
			String today = LocalDate.now(zone).format(DATE_FMT);
			String uidStr = String.valueOf(uid);
			Date now = new Date();

			UserWatchGiftProgressEntity progress = userWatchGiftProgressDao.findOne(uidStr, today);
			if (progress == null) {
				progress = new UserWatchGiftProgressEntity();
				progress.setUid(uidStr);
				progress.setBizDate(today);
				progress.setWatchSeconds(0);
				progress.setClaimedGears("");
				progress.setLastReportTime(null);
				GenericityUtil.setDate(progress);
				try {
					userWatchGiftProgressDao.insert(progress);
				} catch (DuplicateKeyException e) {
					progress = userWatchGiftProgressDao.findOne(uidStr, today);
				}
			}
			if (progress == null) {
				return;
			}

			// 上报间隔：过密则忽略本次数值
			if (minInterval > 0 && progress.getLastReportTime() != null) {
				long elapsedMs = now.getTime() - progress.getLastReportTime().getTime();
				if (elapsedMs < minInterval * 1000L) {
					return;
				}
			}

			int cur = progress.getWatchSeconds() == null ? 0 : progress.getWatchSeconds();
			if (cur >= maxDaily) {
				return;
			}
			int next = Math.min(cur + delta, maxDaily);
			userWatchGiftProgressDao.updateWatchSeconds(progress.getId(), next, now);
		} catch (Exception e) {
			log.warn("addWatchSeconds failed uid={} delta={}: {}", uid, deltaSeconds, e.getMessage());
		}
	}

	private WatchGiftOpResult doClaim(Integer uid, Integer gearIndex) {
		if (gearIndex == null || gearIndex <= 0) {
			return WatchGiftOpResult.fail("watch_gift_gear_invalid");
		}
		WatchGiftGlobalConfigEntity config = watchGiftGlobalConfigDao.findEnabledOne();
		List<WatchGiftRewardConfigEntity> rewards = watchGiftRewardConfigDao.findEnabledList();
		if (config == null || rewards == null || rewards.isEmpty()) {
			return WatchGiftOpResult.fail("watch_gift_disabled");
		}
		WatchGiftRewardConfigEntity gear = watchGiftRewardConfigDao.findEnabledByGear(gearIndex);
		if (gear == null) {
			return WatchGiftOpResult.fail("watch_gift_gear_invalid");
		}
		try {
			String err = claimOne(uid, config, gear);
			if (err != null) {
				return WatchGiftOpResult.fail(err);
			}
			return WatchGiftOpResult.success(buildSummaryInternal(uid, config, rewards), gear.getRewardCoin());
		} catch (Exception e) {
			log.error("watchGift claim failed uid={} gear={}", uid, gearIndex, e);
			throw new RuntimeException(e);
		}
	}

	private WatchGiftOpResult doClaimAll(Integer uid) {
		WatchGiftGlobalConfigEntity config = watchGiftGlobalConfigDao.findEnabledOne();
		List<WatchGiftRewardConfigEntity> rewards = watchGiftRewardConfigDao.findEnabledList();
		if (config == null || rewards == null || rewards.isEmpty()) {
			return WatchGiftOpResult.fail("watch_gift_disabled");
		}
		int totalCoin = 0;
		int claimed = 0;
		try {
			for (WatchGiftRewardConfigEntity gear : rewards) {
				String err = claimOne(uid, config, gear);
				if (err == null) {
					claimed++;
					totalCoin += gear.getRewardCoin() == null ? 0 : gear.getRewardCoin();
				} else if (!"watch_gift_already_claimed".equals(err) && !"watch_gift_not_reach".equals(err)) {
					return WatchGiftOpResult.fail(err);
				}
			}
			if (claimed == 0) {
				return WatchGiftOpResult.fail("watch_gift_nothing_to_claim");
			}
			return WatchGiftOpResult.success(buildSummaryInternal(uid, config, rewards), totalCoin);
		} catch (Exception e) {
			log.error("watchGift claimAll failed uid={}", uid, e);
			throw new RuntimeException(e);
		}
	}

	/** @return null 成功，否则 i18n key */
	private String claimOne(Integer uid, WatchGiftGlobalConfigEntity config, WatchGiftRewardConfigEntity gear)
			throws Exception {
		ZoneId zone = resolveZone(config.getTimezone());
		String today = LocalDate.now(zone).format(DATE_FMT);
		String uidStr = String.valueOf(uid);
		UserWatchGiftProgressEntity progress = userWatchGiftProgressDao.findOne(uidStr, today);
		int watchSeconds = progress == null || progress.getWatchSeconds() == null ? 0 : progress.getWatchSeconds();
		Set<Integer> claimed = parseClaimed(progress == null ? null : progress.getClaimedGears());

		if (claimed.contains(gear.getGearIndex())) {
			return "watch_gift_already_claimed";
		}
		int target = gear.getTargetSeconds() == null ? 0 : gear.getTargetSeconds();
		if (watchSeconds < target) {
			return "watch_gift_not_reach";
		}

		int coin = gear.getRewardCoin() == null ? 0 : gear.getRewardCoin();
		String bizId = BIZ_ID_PREFIX + today + ":" + gear.getGearIndex();
		creditCoin(uid, coin, CoinBizTypeEnums.WATCH_GIFT.getName(), bizId, "WATCH_GIFT",
				"watch gift gear " + gear.getGearIndex());

		if (progress == null) {
			progress = new UserWatchGiftProgressEntity();
			progress.setUid(uidStr);
			progress.setBizDate(today);
			progress.setWatchSeconds(watchSeconds);
			progress.setClaimedGears(String.valueOf(gear.getGearIndex()));
			GenericityUtil.setDate(progress);
			userWatchGiftProgressDao.insert(progress);
		} else {
			claimed.add(gear.getGearIndex());
			String joined = claimed.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
			userWatchGiftProgressDao.updateClaimedGears(progress.getId(), joined);
		}
		return null;
	}

	private WatchGiftHomeSummaryEntity buildSummaryInternal(Integer uid, WatchGiftGlobalConfigEntity config,
			List<WatchGiftRewardConfigEntity> rewards) {
		ZoneId zone = resolveZone(config.getTimezone());
		String today = LocalDate.now(zone).format(DATE_FMT);
		UserWatchGiftProgressEntity progress = userWatchGiftProgressDao.findOne(String.valueOf(uid), today);
		int watchSeconds = progress == null || progress.getWatchSeconds() == null ? 0 : progress.getWatchSeconds();
		Set<Integer> claimed = parseClaimed(progress == null ? null : progress.getClaimedGears());

		List<WatchGiftRewardItemEntity> items = new ArrayList<>();
		Integer nextTarget = null;
		for (WatchGiftRewardConfigEntity gear : rewards) {
			WatchGiftRewardItemEntity item = new WatchGiftRewardItemEntity();
			item.setGearIndex(gear.getGearIndex());
			item.setTargetSeconds(gear.getTargetSeconds());
			int sec = gear.getTargetSeconds() == null ? 0 : gear.getTargetSeconds();
			item.setTargetMinutes((int) Math.ceil(sec / 60.0));
			item.setRewardCoin(gear.getRewardCoin() == null ? 0 : gear.getRewardCoin());
			String state;
			if (claimed.contains(gear.getGearIndex())) {
				state = WatchGiftRewardStateEnums.DONE.getName();
			} else if (watchSeconds >= sec) {
				state = WatchGiftRewardStateEnums.CLAIMABLE.getName();
			} else {
				state = WatchGiftRewardStateEnums.LOCKED.getName();
				if (nextTarget == null) {
					nextTarget = sec;
				}
			}
			item.setState(state);
			items.add(item);
		}

		WatchGiftHomeSummaryEntity summary = new WatchGiftHomeSummaryEntity();
		summary.setToday(today);
		summary.setWatchSeconds(watchSeconds);
		summary.setWatchMinutes(watchSeconds / 60);
		summary.setNextTargetSeconds(nextTarget);
		summary.setRewards(items);
		return summary;
	}

	private Set<Integer> parseClaimed(String claimedGears) {
		Set<Integer> set = new HashSet<>();
		if (StringUtils.isEmpty(claimedGears)) {
			return set;
		}
		Arrays.stream(claimedGears.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.forEach(s -> {
					try {
						set.add(Integer.parseInt(s));
					} catch (NumberFormatException ignored) {
						// skip
					}
				});
		return set;
	}

	private void creditCoin(Integer uid, int amt, String bizType, String bizId, String taskCode, String remark)
			throws Exception {
		if (amt <= 0) {
			return;
		}
		UserCoinLedgerEntity exist = userCoinLedgerDao.findByBiz(uid, bizType, bizId);
		if (exist != null) {
			return;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		long before = account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt(amt);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before + amt);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setTaskCode(taskCode == null ? "" : taskCode);
		ledger.setAdBoostFlag(0);
		ledger.setRemark(remark == null ? "" : remark);
		GenericityUtil.setDate(ledger);
		try {
			userCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			return;
		}
		appAccountDao.addCoinBalance(uid, amt);
	}

	private ZoneId resolveZone(String timezone) {
		try {
			if (!StringUtils.isEmpty(timezone)) {
				return ZoneId.of(timezone.trim());
			}
		} catch (Exception e) {
			log.warn("invalid watch-gift timezone: {}", timezone);
		}
		return ZoneId.of("Asia/Shanghai");
	}
}
