package com.playlet.internal.service.impl;

import com.playlet.internal.api.response.*;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.welfare.*;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.welfare.*;
import com.playlet.internal.enums.*;
import com.playlet.internal.service.SignInService;
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
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 连续签到实现（补签消耗补签卡）。
 *
 * @author GeminiSun
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class SignInServiceImpl extends BaseApiService implements SignInService {

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
	private static final String CYCLE_LOOP = "LOOP";
	private static final String BIZ_ID_SIGN_PREFIX = "SIGN_IN:";
	private static final String BIZ_ID_CARD_BUY_PREFIX = "SIGN_IN_MAKEUP_CARD_BUY:";

	@Autowired
	private SignInGlobalConfigDao signInGlobalConfigDao;
	@Autowired
	private SignInRewardConfigDao signInRewardConfigDao;
	@Autowired
	private UserSignInDao userSignInDao;
	@Autowired
	private UserSignInLogDao userSignInLogDao;
	@Autowired
	private UserCoinLedgerDao userCoinLedgerDao;
	@Autowired
	private AppAccountDao appAccountDao;

	@Override
	public ResponseBase signIn(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		SignInOpResult result = doSignIn(uid);
		if (!result.isOk()) {
			return setResultError(I18nUtil.getMessage(result.getMsgKey()));
		}
		return setResultSuccess(result.getSummary(), I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase makeup(@RequestParam String bizDate, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		SignInOpResult result = doMakeup(uid, bizDate);
		if (!result.isOk()) {
			return setResultError(I18nUtil.getMessage(result.getMsgKey()));
		}
		return setResultSuccess(result.getSummary(), I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase calendar(@RequestParam(required = false) String yearMonth, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		SignInGlobalConfigEntity config = signInGlobalConfigDao.findEnabledOne();
		if (config == null) {
			return setResultError(I18nUtil.getMessage("sign_in_disabled"));
		}
		try {
			return setResultSuccess(buildCalendar(uid, yearMonth, config), I18nUtil.getMessage("base_success"));
		} catch (DateTimeParseException e) {
			return setResultError(I18nUtil.getMessage("sign_in_calendar_month_invalid"));
		}
	}

	@Override
	public ResponseBase buyMakeupCard(@RequestParam(required = false, defaultValue = "1") Integer count,
			HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		SignInOpResult result = doBuyMakeupCard(uid, count == null ? 1 : count);
		if (!result.isOk()) {
			return setResultError(I18nUtil.getMessage(result.getMsgKey()));
		}
		return setResultSuccess(result.getSummary(), I18nUtil.getMessage("base_success"));
	}

	@Override
	public SignInHomeSummaryEntity buildHomeSummary(Integer uid) {
		if (uid == null) {
			return null;
		}
		SignInGlobalConfigEntity config = signInGlobalConfigDao.findEnabledOne();
		List<SignInRewardConfigEntity> rewardConfigs = signInRewardConfigDao.findEnabledList();
		if (config == null || rewardConfigs == null || rewardConfigs.isEmpty()) {
			return null;
		}
		return buildHomeSummaryInternal(uid, config, rewardConfigs);
	}

	@Override
	public boolean grantMakeupCards(Integer uid, int count, String remark) {
		if (uid == null || count <= 0) {
			return false;
		}
		try {
			ensureUserSignRow(String.valueOf(uid));
			return userSignInDao.addMakeupCardBalance(String.valueOf(uid), count) > 0;
		} catch (Exception e) {
			log.error("grantMakeupCards failed uid={} count={}", uid, count, e);
			return false;
		}
	}

	private SignInOpResult doSignIn(Integer uid) {
		SignInGlobalConfigEntity config = signInGlobalConfigDao.findEnabledOne();
		List<SignInRewardConfigEntity> rewardConfigs = signInRewardConfigDao.findEnabledList();
		if (config == null || rewardConfigs == null || rewardConfigs.isEmpty()) {
			return SignInOpResult.fail("sign_in_disabled");
		}

		ZoneId zone = resolveZone(config.getTimezone());
		LocalDate todayDate = LocalDate.now(zone);
		String today = todayDate.format(DATE_FMT);
		String yesterday = todayDate.minusDays(1).format(DATE_FMT);
		String uidStr = String.valueOf(uid);

		if (userSignInLogDao.findOne(uidStr, today) != null) {
			return SignInOpResult.fail("sign_in_already");
		}

		UserSignInEntity userSign = userSignInDao.findByUid(uidStr);
		int streakDays = userSign == null || userSign.getStreakDays() == null ? 0 : userSign.getStreakDays();
		String lastSignDate = userSign == null ? null : userSign.getLastSignDate();
		int nextStreak = resolveNextStreak(lastSignDate, yesterday, streakDays);
		int dayIndex = resolveDayIndex(nextStreak, config, rewardConfigs);
		int rewardCoin = resolveRewardCoin(dayIndex, rewardConfigs);

		try {
			insertSignLog(uidStr, today, SignInTypeEnums.NORMAL.getCode(), nextStreak, dayIndex, rewardCoin, 0, 0);
			creditCoin(uid, rewardCoin, CoinBizTypeEnums.SIGN_IN.getName(), BIZ_ID_SIGN_PREFIX + today,
					WelfareTaskCodeEnums.SIGN_IN.getCode(), 0, "sign in " + today);
			upsertUserSignIn(uidStr, userSign, nextStreak, today, 1);
		} catch (DuplicateKeyException e) {
			return SignInOpResult.fail("sign_in_already");
		} catch (Exception e) {
			log.error("signIn failed uid={}", uid, e);
			throw new RuntimeException(e);
		}

		return SignInOpResult.success(buildHomeSummaryInternal(uid, config, rewardConfigs), rewardCoin, 0);
	}

	/**
	 * 补签：窗口校验 → 扣补签卡 → 按补签日连续档发奖 → 重算 streak。
	 */
	private SignInOpResult doMakeup(Integer uid, String bizDate) {
		if (StringUtils.isEmpty(bizDate)) {
			return SignInOpResult.fail("sign_in_makeup_date_required");
		}
		SignInGlobalConfigEntity config = signInGlobalConfigDao.findEnabledOne();
		List<SignInRewardConfigEntity> rewardConfigs = signInRewardConfigDao.findEnabledList();
		if (config == null || rewardConfigs == null || rewardConfigs.isEmpty()) {
			return SignInOpResult.fail("sign_in_disabled");
		}
		if (config.getMakeupEnabled() == null || config.getMakeupEnabled() != 1) {
			return SignInOpResult.fail("sign_in_makeup_disabled");
		}

		ZoneId zone = resolveZone(config.getTimezone());
		LocalDate todayDate = LocalDate.now(zone);
		LocalDate targetDate;
		try {
			targetDate = LocalDate.parse(bizDate.trim(), DATE_FMT);
		} catch (DateTimeParseException e) {
			return SignInOpResult.fail("sign_in_makeup_date_invalid");
		}
		String target = targetDate.format(DATE_FMT);
		if (!targetDate.isBefore(todayDate)) {
			return SignInOpResult.fail("sign_in_makeup_date_invalid");
		}
		int window = config.getMakeupWindowDays() == null ? 0 : config.getMakeupWindowDays();
		if (window <= 0 || targetDate.isBefore(todayDate.minusDays(window))) {
			return SignInOpResult.fail("sign_in_makeup_out_of_window");
		}

		String uidStr = String.valueOf(uid);
		if (userSignInLogDao.findOne(uidStr, target) != null) {
			return SignInOpResult.fail("sign_in_makeup_already");
		}
		// 可选：每月补签次数上限（>0 时生效）
		if (config.getMakeupMonthLimit() != null && config.getMakeupMonthLimit() > 0
				&& calcMakeupRemain(uidStr, config, todayDate) <= 0) {
			return SignInOpResult.fail("sign_in_makeup_limit");
		}

		int costCard = resolveMakeupCostCard(config);
		try {
			ensureUserSignRow(uidStr);
			if (userSignInDao.addMakeupCardBalance(uidStr, -costCard) <= 0) {
				return SignInOpResult.fail("sign_in_makeup_card_not_enough");
			}

			int streakAtTarget = calcStreakEndingAt(uidStr, targetDate, todayDate, true);
			int dayIndex = resolveDayIndex(streakAtTarget, config, rewardConfigs);
			int rewardCoin = resolveRewardCoin(dayIndex, rewardConfigs);

			insertSignLog(uidStr, target, SignInTypeEnums.MAKEUP.getCode(), streakAtTarget, dayIndex, rewardCoin,
					0, costCard);
			creditCoin(uid, rewardCoin, CoinBizTypeEnums.SIGN_IN.getName(), BIZ_ID_SIGN_PREFIX + target,
					WelfareTaskCodeEnums.SIGN_IN.getCode(), 0, "sign in makeup " + target);

			UserSignInEntity userSign = userSignInDao.findByUid(uidStr);
			int newStreak = calcStreakEndingAt(uidStr, resolveLastSignedDate(uidStr, todayDate), todayDate, false);
			String lastSignDate = resolveLastSignedDateStr(uidStr, todayDate);
			upsertUserSignIn(uidStr, userSign, newStreak, lastSignDate, 1);
			return SignInOpResult.success(buildHomeSummaryInternal(uid, config, rewardConfigs), rewardCoin, 0);
		} catch (DuplicateKeyException e) {
			return SignInOpResult.fail("sign_in_makeup_already");
		} catch (Exception e) {
			log.error("makeup failed uid={} date={}", uid, target, e);
			throw new RuntimeException(e);
		}
	}

	private SignInOpResult doBuyMakeupCard(Integer uid, int count) {
		if (count <= 0) {
			return SignInOpResult.fail("sign_in_makeup_buy_count_invalid");
		}
		SignInGlobalConfigEntity config = signInGlobalConfigDao.findEnabledOne();
		List<SignInRewardConfigEntity> rewardConfigs = signInRewardConfigDao.findEnabledList();
		if (config == null) {
			return SignInOpResult.fail("sign_in_disabled");
		}
		int price = config.getMakeupBuyPriceCoin() == null ? 0 : config.getMakeupBuyPriceCoin();
		if (price <= 0) {
			return SignInOpResult.fail("sign_in_makeup_buy_disabled");
		}
		int monthLimit = config.getMakeupBuyMonthLimit() == null ? 0 : config.getMakeupBuyMonthLimit();
		ZoneId zone = resolveZone(config.getTimezone());
		LocalDate todayDate = LocalDate.now(zone);
		String month = todayDate.format(MONTH_FMT);
		String uidStr = String.valueOf(uid);

		try {
			ensureUserSignRow(uidStr);
			UserSignInEntity userSign = userSignInDao.findByUid(uidStr);
			int used = resolveBuyUsed(userSign, month);
			if (monthLimit > 0 && used + count > monthLimit) {
				return SignInOpResult.fail("sign_in_makeup_buy_limit");
			}
			int totalCost = price * count;
			boolean deducted = deductCoin(uid, totalCost, CoinBizTypeEnums.CONSUME.getName(),
					BIZ_ID_CARD_BUY_PREFIX + month + ":" + (used + count),
					WelfareTaskCodeEnums.SIGN_IN.getCode(), "buy makeup card x" + count);
			if (!deducted) {
				return SignInOpResult.fail("sign_in_makeup_coin_not_enough");
			}
			userSignInDao.addMakeupCardBalance(uidStr, count);
			userSignInDao.updateMakeupBuyStat(uidStr, month, used + count);
			SignInHomeSummaryEntity summary = rewardConfigs == null || rewardConfigs.isEmpty()
					? null
					: buildHomeSummaryInternal(uid, config, rewardConfigs);
			return SignInOpResult.success(summary, 0, totalCost);
		} catch (Exception e) {
			log.error("buyMakeupCard failed uid={} count={}", uid, count, e);
			throw new RuntimeException(e);
		}
	}

	private SignInCalendarRespEntity buildCalendar(Integer uid, String yearMonth, SignInGlobalConfigEntity config) {
		ZoneId zone = resolveZone(config.getTimezone());
		LocalDate todayDate = LocalDate.now(zone);
		YearMonth ym;
		if (StringUtils.isEmpty(yearMonth)) {
			ym = YearMonth.from(todayDate);
		} else {
			ym = YearMonth.parse(yearMonth.trim(), MONTH_FMT);
		}
		String uidStr = String.valueOf(uid);
		UserSignInEntity userSign = userSignInDao.findByUid(uidStr);
		String monthKey = ym.format(MONTH_FMT);

		LocalDate start = ym.atDay(1);
		LocalDate end = ym.atEndOfMonth();
		List<UserSignInLogEntity> logs = userSignInLogDao.findByUidAndDateRange(uidStr,
				start.format(DATE_FMT), end.format(DATE_FMT));
		Set<String> signed = new HashSet<>();
		if (logs != null) {
			for (UserSignInLogEntity row : logs) {
				if (row != null && !StringUtils.isEmpty(row.getBizDate())) {
					signed.add(row.getBizDate());
				}
			}
		}

		int window = config.getMakeupWindowDays() == null ? 0 : config.getMakeupWindowDays();
		boolean makeupOn = config.getMakeupEnabled() != null && config.getMakeupEnabled() == 1;
		LocalDate windowStart = window > 0 ? todayDate.minusDays(window) : todayDate;

		List<SignInCalendarDayEntity> days = new ArrayList<>();
		for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
			SignInCalendarDayEntity day = new SignInCalendarDayEntity();
			String bizDate = d.format(DATE_FMT);
			day.setBizDate(bizDate);
			day.setDay(d.getDayOfMonth());
			if (signed.contains(bizDate)) {
				day.setState(SignInCalendarDayStateEnums.SIGNED.getName());
			} else if (d.equals(todayDate)) {
				day.setState(SignInCalendarDayStateEnums.TODAY.getName());
			} else if (makeupOn && d.isBefore(todayDate) && !d.isBefore(windowStart)) {
				day.setState(SignInCalendarDayStateEnums.MAKEUP.getName());
			} else {
				day.setState(SignInCalendarDayStateEnums.EMPTY.getName());
			}
			days.add(day);
		}

		SignInCalendarRespEntity resp = new SignInCalendarRespEntity();
		resp.setYearMonth(monthKey);
		resp.setToday(todayDate.format(DATE_FMT));
		resp.setMakeupCardBalance(userSign == null || userSign.getMakeupCardBalance() == null
				? 0 : userSign.getMakeupCardBalance());
		resp.setMakeupCostCard(resolveMakeupCostCard(config));
		resp.setMakeupBuyUsed(resolveBuyUsed(userSign, monthKey));
		resp.setMakeupBuyMonthLimit(config.getMakeupBuyMonthLimit() == null ? 0 : config.getMakeupBuyMonthLimit());
		resp.setMakeupBuyPriceCoin(config.getMakeupBuyPriceCoin() == null ? 0 : config.getMakeupBuyPriceCoin());
		resp.setDays(days);
		return resp;
	}

	private SignInHomeSummaryEntity buildHomeSummaryInternal(Integer uid, SignInGlobalConfigEntity config,
			List<SignInRewardConfigEntity> rewardConfigs) {
		ZoneId zone = resolveZone(config.getTimezone());
		LocalDate todayDate = LocalDate.now(zone);
		String today = todayDate.format(DATE_FMT);
		String yesterday = todayDate.minusDays(1).format(DATE_FMT);
		String month = todayDate.format(MONTH_FMT);
		String uidStr = String.valueOf(uid);

		UserSignInEntity userSign = userSignInDao.findByUid(uidStr);
		UserSignInLogEntity todayLog = userSignInLogDao.findOne(uidStr, today);
		boolean todaySigned = todayLog != null;

		int streakDays = userSign == null || userSign.getStreakDays() == null ? 0 : userSign.getStreakDays();
		int totalSignDays = userSign == null || userSign.getTotalSignDays() == null ? 0 : userSign.getTotalSignDays();
		String lastSignDate = userSign == null ? null : userSign.getLastSignDate();

		int displayStreak = streakDays;
		if (!todaySigned) {
			if (StringUtils.isEmpty(lastSignDate) || (!yesterday.equals(lastSignDate) && !today.equals(lastSignDate))) {
				displayStreak = 0;
			}
		}

		int todayRewardDayIndex;
		int todayRewardCoin;
		if (todaySigned) {
			todayRewardDayIndex = todayLog.getRewardDayIndex() == null
					? resolveDayIndex(streakDays, config, rewardConfigs)
					: todayLog.getRewardDayIndex();
			todayRewardCoin = todayLog.getRewardCoin() == null
					? resolveRewardCoin(todayRewardDayIndex, rewardConfigs)
					: todayLog.getRewardCoin();
		} else {
			int nextStreak = resolveNextStreak(lastSignDate, yesterday, streakDays);
			todayRewardDayIndex = resolveDayIndex(nextStreak, config, rewardConfigs);
			todayRewardCoin = resolveRewardCoin(todayRewardDayIndex, rewardConfigs);
		}

		SignInHomeSummaryEntity summary = new SignInHomeSummaryEntity();
		summary.setToday(today);
		summary.setTodaySigned(todaySigned);
		summary.setStreakDays(displayStreak);
		summary.setTotalSignDays(totalSignDays);
		summary.setTodayRewardDayIndex(todayRewardDayIndex);
		summary.setTodayRewardCoin(todayRewardCoin);
		summary.setMakeupEnabled(config.getMakeupEnabled() != null && config.getMakeupEnabled() == 1);
		if (config.getMakeupMonthLimit() == null || config.getMakeupMonthLimit() <= 0) {
			summary.setMakeupRemainCount(null);
		} else {
			summary.setMakeupRemainCount(calcMakeupRemain(uidStr, config, todayDate));
		}
		summary.setMakeupCardBalance(userSign == null || userSign.getMakeupCardBalance() == null
				? 0 : userSign.getMakeupCardBalance());
		summary.setMakeupCostCard(resolveMakeupCostCard(config));
		summary.setMakeupBuyUsed(resolveBuyUsed(userSign, month));
		summary.setMakeupBuyMonthLimit(config.getMakeupBuyMonthLimit() == null ? 0 : config.getMakeupBuyMonthLimit());
		summary.setMakeupBuyPriceCoin(config.getMakeupBuyPriceCoin() == null ? 0 : config.getMakeupBuyPriceCoin());
		summary.setRewards(buildRewardItems(rewardConfigs, todaySigned, todayRewardDayIndex));
		return summary;
	}

	private void insertSignLog(String uid, String bizDate, int signType, int streakDays, int dayIndex,
			int rewardCoin, int costCoin, int costCard) {
		UserSignInLogEntity logRow = new UserSignInLogEntity();
		logRow.setUid(uid);
		logRow.setBizDate(bizDate);
		logRow.setSignType(signType);
		logRow.setStreakDays(streakDays);
		logRow.setRewardDayIndex(dayIndex);
		logRow.setRewardCoin(rewardCoin);
		logRow.setCostCoin(costCoin);
		logRow.setCostCard(costCard);
		logRow.setAdFlag(0);
		logRow.setAdTicket("");
		logRow.setSetTime(new Date());
		userSignInLogDao.insert(logRow);
	}

	private void upsertUserSignIn(String uid, UserSignInEntity exist, int streakDays, String lastSignDate,
			int addTotalDays) throws Exception {
		int total = (exist == null || exist.getTotalSignDays() == null ? 0 : exist.getTotalSignDays()) + addTotalDays;
		if (exist == null) {
			UserSignInEntity row = new UserSignInEntity();
			row.setUid(uid);
			row.setStreakDays(streakDays);
			row.setLastSignDate(lastSignDate);
			row.setTotalSignDays(total);
			row.setMakeupCardBalance(0);
			row.setMakeupBuyCount(0);
			GenericityUtil.setDate(row);
			userSignInDao.insert(row);
		} else {
			userSignInDao.updateStreak(uid, streakDays, lastSignDate, total);
		}
	}

	/** 确保用户签到摘要行存在（用于持有补签卡） */
	private void ensureUserSignRow(String uid) throws Exception {
		if (userSignInDao.findByUid(uid) != null) {
			return;
		}
		UserSignInEntity row = new UserSignInEntity();
		row.setUid(uid);
		row.setStreakDays(0);
		row.setLastSignDate(null);
		row.setTotalSignDays(0);
		row.setMakeupCardBalance(0);
		row.setMakeupBuyCount(0);
		GenericityUtil.setDate(row);
		try {
			userSignInDao.insert(row);
		} catch (DuplicateKeyException ignored) {
			// 并发下已插入
		}
	}

	private int calcStreakEndingAt(String uid, LocalDate endDate, LocalDate todayDate, boolean includeVirtual) {
		if (endDate == null) {
			return 0;
		}
		LocalDate rangeStart = todayDate.minusDays(400);
		List<UserSignInLogEntity> logs = userSignInLogDao.findByUidAndDateRange(uid,
				rangeStart.format(DATE_FMT), todayDate.format(DATE_FMT));
		Set<String> signed = new HashSet<>();
		if (logs != null) {
			for (UserSignInLogEntity row : logs) {
				if (row != null && !StringUtils.isEmpty(row.getBizDate())) {
					signed.add(row.getBizDate());
				}
			}
		}
		if (includeVirtual) {
			signed.add(endDate.format(DATE_FMT));
		}
		int streak = 0;
		LocalDate cursor = endDate;
		while (signed.contains(cursor.format(DATE_FMT))) {
			streak++;
			cursor = cursor.minusDays(1);
		}
		return streak;
	}

	private LocalDate resolveLastSignedDate(String uid, LocalDate todayDate) {
		String last = resolveLastSignedDateStr(uid, todayDate);
		if (StringUtils.isEmpty(last)) {
			return todayDate;
		}
		return LocalDate.parse(last, DATE_FMT);
	}

	private String resolveLastSignedDateStr(String uid, LocalDate todayDate) {
		LocalDate rangeStart = todayDate.minusDays(400);
		List<UserSignInLogEntity> logs = userSignInLogDao.findByUidAndDateRange(uid,
				rangeStart.format(DATE_FMT), todayDate.format(DATE_FMT));
		String max = null;
		if (logs != null) {
			for (UserSignInLogEntity row : logs) {
				if (row == null || StringUtils.isEmpty(row.getBizDate())) {
					continue;
				}
				if (max == null || row.getBizDate().compareTo(max) > 0) {
					max = row.getBizDate();
				}
			}
		}
		return max;
	}

	private void creditCoin(Integer uid, int amt, String bizType, String bizId, String taskCode,
			int adBoostFlag, String remark) throws Exception {
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
		ledger.setAdBoostFlag(adBoostFlag);
		ledger.setRemark(remark == null ? "" : remark);
		GenericityUtil.setDate(ledger);
		try {
			userCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			return;
		}
		appAccountDao.addCoinBalance(uid, amt);
	}

	private boolean deductCoin(Integer uid, int amt, String bizType, String bizId, String taskCode, String remark)
			throws Exception {
		if (amt <= 0) {
			return true;
		}
		UserCoinLedgerEntity exist = userCoinLedgerDao.findByBiz(uid, bizType, bizId);
		if (exist != null) {
			return true;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		long before = account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		if (before < amt) {
			return false;
		}
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt(-amt);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before - amt);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setTaskCode(taskCode == null ? "" : taskCode);
		ledger.setAdBoostFlag(0);
		ledger.setRemark(remark == null ? "" : remark);
		GenericityUtil.setDate(ledger);
		try {
			userCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			return true;
		}
		return appAccountDao.deductCoinBalance(uid, amt) > 0;
	}

	private List<SignInRewardItemEntity> buildRewardItems(List<SignInRewardConfigEntity> rewardConfigs,
			boolean todaySigned, int todayRewardDayIndex) {
		List<SignInRewardItemEntity> items = new ArrayList<>();
		for (SignInRewardConfigEntity cfg : rewardConfigs) {
			if (cfg == null || cfg.getDayIndex() == null) {
				continue;
			}
			SignInRewardItemEntity item = new SignInRewardItemEntity();
			item.setDayIndex(cfg.getDayIndex());
			item.setRewardCoin(cfg.getRewardCoin() == null ? 0 : cfg.getRewardCoin());
			item.setState(resolveRewardState(cfg.getDayIndex(), todaySigned, todayRewardDayIndex).getName());
			items.add(item);
		}
		return items;
	}

	private SignInRewardStateEnums resolveRewardState(int dayIndex, boolean todaySigned, int todayRewardDayIndex) {
		if (dayIndex < todayRewardDayIndex) {
			return SignInRewardStateEnums.DONE;
		}
		if (dayIndex == todayRewardDayIndex) {
			return todaySigned ? SignInRewardStateEnums.DONE : SignInRewardStateEnums.TODAY;
		}
		return SignInRewardStateEnums.LOCKED;
	}

	private int resolveNextStreak(String lastSignDate, String yesterday, int streakDays) {
		if (!StringUtils.isEmpty(lastSignDate) && yesterday.equals(lastSignDate)) {
			return Math.max(streakDays, 0) + 1;
		}
		return 1;
	}

	private int resolveDayIndex(int streak, SignInGlobalConfigEntity config,
			List<SignInRewardConfigEntity> rewardConfigs) {
		int maxDay = 0;
		for (SignInRewardConfigEntity cfg : rewardConfigs) {
			if (cfg.getDayIndex() != null && cfg.getDayIndex() > maxDay) {
				maxDay = cfg.getDayIndex();
			}
		}
		if (maxDay <= 0) {
			return 1;
		}
		int safeStreak = Math.max(streak, 1);
		String mode = config.getCycleMode() == null ? "CAP" : config.getCycleMode().trim();
		int cycleDays = config.getCycleDays() == null || config.getCycleDays() <= 0 ? maxDay : config.getCycleDays();
		if (CYCLE_LOOP.equalsIgnoreCase(mode)) {
			return ((safeStreak - 1) % cycleDays) + 1;
		}
		return Math.min(safeStreak, maxDay);
	}

	private int resolveRewardCoin(int dayIndex, List<SignInRewardConfigEntity> rewardConfigs) {
		for (SignInRewardConfigEntity cfg : rewardConfigs) {
			if (cfg.getDayIndex() != null && cfg.getDayIndex() == dayIndex) {
				return cfg.getRewardCoin() == null ? 0 : cfg.getRewardCoin();
			}
		}
		int coin = 0;
		int maxDay = 0;
		for (SignInRewardConfigEntity cfg : rewardConfigs) {
			if (cfg.getDayIndex() != null && cfg.getDayIndex() > maxDay) {
				maxDay = cfg.getDayIndex();
				coin = cfg.getRewardCoin() == null ? 0 : cfg.getRewardCoin();
			}
		}
		return coin;
	}

	private int resolveMakeupCostCard(SignInGlobalConfigEntity config) {
		int cost = config.getMakeupCostCard() == null ? 1 : config.getMakeupCostCard();
		return Math.max(cost, 1);
	}

	private int resolveBuyUsed(UserSignInEntity userSign, String month) {
		if (userSign == null || StringUtils.isEmpty(month)) {
			return 0;
		}
		if (!month.equals(userSign.getMakeupBuyMonth())) {
			return 0;
		}
		return userSign.getMakeupBuyCount() == null ? 0 : userSign.getMakeupBuyCount();
	}

	/**
	 * 本月剩余补签次数；仅当 makeup_month_limit &gt; 0 时有意义。
	 */
	private int calcMakeupRemain(String uid, SignInGlobalConfigEntity config, LocalDate todayDate) {
		int limit = config.getMakeupMonthLimit() == null ? 0 : config.getMakeupMonthLimit();
		if (limit <= 0) {
			return 0;
		}
		YearMonth ym = YearMonth.from(todayDate);
		String start = ym.atDay(1).format(DATE_FMT);
		String end = ym.atEndOfMonth().format(DATE_FMT);
		int used = userSignInLogDao.countByUidAndTypeAndDateRange(uid, SignInTypeEnums.MAKEUP.getCode(), start, end);
		return Math.max(limit - used, 0);
	}

	private ZoneId resolveZone(String timezone) {
		try {
			if (!StringUtils.isEmpty(timezone)) {
				return ZoneId.of(timezone.trim());
			}
		} catch (Exception e) {
			log.warn("invalid sign-in timezone: {}", timezone);
		}
		return ZoneId.of("Asia/Shanghai");
	}
}
