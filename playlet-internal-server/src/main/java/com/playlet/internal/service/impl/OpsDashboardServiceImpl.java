package com.playlet.internal.service.impl;

import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.response.OnlineCountRespEntity;
import com.playlet.internal.api.response.OpsDashboardSummaryResp;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.OpsDashboardConstants;
import com.playlet.internal.constants.RankBoardGroupConstants;
import com.playlet.internal.dao.ops.OpsStatDao;
import com.playlet.internal.dao.ops.UserActiveDailyDao;
import com.playlet.internal.dao.ops.UserPlayDailyDao;
import com.playlet.internal.query.ops.OpsDashboardQuery;
import com.playlet.internal.service.OpsDashboardService;
import com.playlet.internal.service.support.UserOnlineHeartbeatService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 运营看板：在线 + 可落地用户/播放指标汇总。
 */
@Slf4j
@RestController
@CrossOrigin
public class OpsDashboardServiceImpl implements OpsDashboardService {

	@Autowired
	private UserOnlineHeartbeatService userOnlineHeartbeatService;
	@Autowired
	private OpsStatDao opsStatDao;
	@Autowired
	private UserActiveDailyDao userActiveDailyDao;
	@Autowired
	private UserPlayDailyDao userPlayDailyDao;

	@Override
	@SysLogAnnotation(module = "运营数据看板", type = "POST", remark = "看板指标汇总")
	public ResponseBase summary(@RequestBody(required = false) OpsDashboardQuery query) {
		if (query == null) {
			query = new OpsDashboardQuery();
		}
		LocalDate[] range;
		try {
			range = resolveRange(query);
		} catch (IllegalArgumentException e) {
			return setResultError(e.getMessage());
		}
		LocalDate start = range[0];
		LocalDate end = range[1];
		String startDate = start.format(RankBoardGroupConstants.DATE_FMT);
		String endDate = end.format(RankBoardGroupConstants.DATE_FMT);
		String startTime = startDate + " 00:00:00";
		String endExclusive = end.plusDays(1).format(RankBoardGroupConstants.DATE_FMT) + " 00:00:00";

		// 新用户人数
		long newUserCnt = opsStatDao.countNewUsers(startTime, endExclusive);
		// 今日播放时长
		long totalPlaySeconds = userPlayDailyDao.sumPlaySeconds(startDate, endDate);
		// 用户日播放尚无打点时，回退剧维度日表
		if (totalPlaySeconds <= 0) {
			totalPlaySeconds = opsStatDao.sumDramaPlaySeconds(startDate, endDate);
		}
		// 每日 DAU（日活跃用户） 之和
		long personDays = userActiveDailyDao.sumPersonDays(startDate, endDate);
		long daySpan = ChronoUnit.DAYS.between(start, end) + 1;
		// 今日 DAU 或 区间平均日活
		long dau;
		if (daySpan <= 1) {
			dau = userActiveDailyDao.countByDate(startDate);
		} else {
			dau = personDays <= 0 ? 0L
					: BigDecimal.valueOf(personDays)
					.divide(BigDecimal.valueOf(daySpan), 0, RoundingMode.HALF_UP)
					.longValue();
		}

		// 次日留存
		BigDecimal retention = resolveRetentionD1(start, end);
		// 人均播放分钟
		BigDecimal avgPlayMinutes = BigDecimal.ZERO;
		if (personDays > 0 && totalPlaySeconds > 0) {
			avgPlayMinutes = BigDecimal.valueOf(totalPlaySeconds)
					.divide(BigDecimal.valueOf(personDays).multiply(BigDecimal.valueOf(60)), 1, RoundingMode.HALF_UP);
		}

		// 在线人数
		OnlineCountRespEntity online = userOnlineHeartbeatService.countOnline();
		Long onlineCount = online.getOnlineCount() == null ? 0L : online.getOnlineCount();
		Long onlineWindow = online.getWindowSeconds() == null ? 0L : online.getWindowSeconds();

		// 响应（对齐看板卡片；GMV/eCPM/ARPU 暂未实现，固定 0）
		OpsDashboardSummaryResp resp = new OpsDashboardSummaryResp();
		resp.setRangeType(normalizeRangeType(query.getRangeType()));
		resp.setStartDate(startDate);
		resp.setEndDate(endDate);
		resp.setGmv(BigDecimal.ZERO);
		resp.setAdEcpm(BigDecimal.ZERO);
		resp.setArpu(BigDecimal.ZERO);
		resp.setDau(dau);
		resp.setOnlineCount(onlineCount);
		resp.setOnlineWindowSeconds(onlineWindow);
		resp.setRetentionD1Rate(retention == null ? BigDecimal.ZERO : retention);
		resp.setNewUserCnt(newUserCnt);
		resp.setAvgPlayMinutes(avgPlayMinutes);
		resp.setTotalPlaySeconds(totalPlaySeconds);
		resp.setPersonDays(personDays);
		log.info("ops dashboard summary range={}~{} newUser={} playSec={} dau={} retention={} online={}",
				startDate, endDate, newUserCnt, totalPlaySeconds, dau, retention, onlineCount);
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 今日：昨日新增今日回访；多日：start～min(end,昨天) 的 cohort 次日留存均值。
	 */
	private BigDecimal resolveRetentionD1(LocalDate start, LocalDate end) {
		LocalDate today = LocalDate.now(ZoneId.of(RankBoardGroupConstants.TIMEZONE));
		LocalDate cohortStart;
		LocalDate cohortEndInclusive;
		if (start.equals(end) && start.equals(today)) {
			// 今日卡片：看「昨天新增」的次日留存
			cohortStart = today.minusDays(1);
			cohortEndInclusive = today.minusDays(1);
		} else {
			cohortStart = start;
			// 次日须已发生：cohort 最晚到昨天
			LocalDate maxCohort = today.minusDays(1);
			cohortEndInclusive = end.isBefore(maxCohort) ? end : maxCohort;
			if (cohortEndInclusive.isBefore(cohortStart)) {
				return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
			}
		}
		String cs = cohortStart.format(RankBoardGroupConstants.DATE_FMT) + " 00:00:00";
		String ce = cohortEndInclusive.plusDays(1).format(RankBoardGroupConstants.DATE_FMT) + " 00:00:00";
		BigDecimal rate = userActiveDailyDao.avgRetentionD1Rate(cs, ce);
		if (rate == null) {
			return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
		}
		return rate.setScale(1, RoundingMode.HALF_UP);
	}

	private LocalDate[] resolveRange(OpsDashboardQuery query) {
		LocalDate today = LocalDate.now(ZoneId.of(RankBoardGroupConstants.TIMEZONE));
		String type = normalizeRangeType(query.getRangeType());
		if (OpsDashboardConstants.RANGE_TODAY.equals(type)) {
			return new LocalDate[]{today, today};
		}
		if (OpsDashboardConstants.RANGE_7D.equals(type)) {
			return new LocalDate[]{today.minusDays(OpsDashboardConstants.DAYS_7 - 1L), today};
		}
		if (OpsDashboardConstants.RANGE_30D.equals(type)) {
			return new LocalDate[]{today.minusDays(OpsDashboardConstants.DAYS_30 - 1L), today};
		}
		if (OpsDashboardConstants.RANGE_CUSTOM.equals(type)) {
			if (StringUtils.isEmpty(query.getStartDate()) || StringUtils.isEmpty(query.getEndDate())) {
				throw new IllegalArgumentException(I18nUtil.getMessage("base_error"));
			}
			LocalDate start;
			LocalDate end;
			try {
				start = LocalDate.parse(query.getStartDate().trim(), RankBoardGroupConstants.DATE_FMT);
				end = LocalDate.parse(query.getEndDate().trim(), RankBoardGroupConstants.DATE_FMT);
			} catch (DateTimeParseException e) {
				throw new IllegalArgumentException(I18nUtil.getMessage("base_error"));
			}
			if (end.isBefore(start)) {
				throw new IllegalArgumentException(I18nUtil.getMessage("base_error"));
			}
			long days = ChronoUnit.DAYS.between(start, end) + 1;
			if (days > OpsDashboardConstants.MAX_CUSTOM_DAYS) {
				throw new IllegalArgumentException(I18nUtil.getMessage("base_error"));
			}
			return new LocalDate[]{start, end};
		}
		throw new IllegalArgumentException(I18nUtil.getMessage("base_error"));
	}

	private static String normalizeRangeType(String rangeType) {
		if (StringUtils.isEmpty(rangeType)) {
			return OpsDashboardConstants.RANGE_TODAY;
		}
		return rangeType.trim().toLowerCase();
	}
}
