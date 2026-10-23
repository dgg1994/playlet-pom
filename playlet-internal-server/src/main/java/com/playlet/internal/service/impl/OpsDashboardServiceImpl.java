package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.response.OpsDashboardHotDramaItem;
import com.playlet.internal.api.response.OpsDashboardRevenuePart;
import com.playlet.internal.api.response.OpsDashboardSummaryResp;
import com.playlet.internal.api.response.OpsDashboardTodoItem;
import com.playlet.internal.api.response.OpsDashboardTrendPoint;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.OpsDashboardConstants;
import com.playlet.internal.constants.RankBoardGroupConstants;
import com.playlet.internal.dao.ops.OpsStatDao;
import com.playlet.internal.dao.ops.UserActiveDailyDao;
import com.playlet.internal.query.ops.OpsDashboardHotDramaQuery;
import com.playlet.internal.query.ops.OpsDashboardQuery;
import com.playlet.internal.service.OpsDashboardService;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 运营数据总览：KPI + 趋势 + 收入构成 + 热播榜 + 待办。
 */
@Slf4j
@RestController
@CrossOrigin
public class OpsDashboardServiceImpl implements OpsDashboardService {

	private static final DateTimeFormatter LABEL_FMT = DateTimeFormatter.ofPattern("MM-dd");
	private static final BigDecimal WAN = BigDecimal.valueOf(10000);

	@Autowired
	private OpsStatDao opsStatDao;
	@Autowired
	private UserActiveDailyDao userActiveDailyDao;

	@Override
	@SysLogAnnotation(module = "运营数据看板", type = "POST", remark = "运营数据总览")
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

		LocalDate today = LocalDate.now(ZoneId.of(RankBoardGroupConstants.TIMEZONE));
		LocalDate yesterday = today.minusDays(1);
		String todayStart = today.format(RankBoardGroupConstants.DATE_FMT) + " 00:00:00";
		String todayEndEx = today.plusDays(1).format(RankBoardGroupConstants.DATE_FMT) + " 00:00:00";
		String ydayStart = yesterday.format(RankBoardGroupConstants.DATE_FMT) + " 00:00:00";
		String ydayEndEx = todayStart;

		OpsDashboardSummaryResp resp = new OpsDashboardSummaryResp();
		resp.setRangeType(normalizeRangeType(query.getRangeType()));
		resp.setStartDate(startDate);
		resp.setEndDate(endDate);
		resp.setUpdateTime(new Date());

		fillTodayKpi(resp, today, todayStart, todayEndEx, ydayStart, ydayEndEx);
		resp.setTrendList(buildTrend(start, end, startTime, endExclusive));
		resp.setRevenueParts(buildRevenueParts(startTime, endExclusive));
		resp.setHotDramaTop5(buildHotTop5(startDate, endDate, todayStart, todayEndEx));
		try {
			resp.setTodoList(buildTodos(resp.getPendingAuditDramaCnt()));
		} catch (Exception e) {
			// 待办依赖退款申请表等，缺表时不阻断整页
			log.error("ops dashboard todos failed", e);
			resp.setTodoList(new ArrayList<>());
		}

		log.info("ops dashboard summary range={}~{} todayRecharge={} newUser={} paying={}",
				startDate, endDate, resp.getTodayRechargeAmount(), resp.getTodayNewUserCnt(),
				resp.getPayingUserCnt());
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "运营数据看板", type = "POST", remark = "热播短剧排行")
	public ResponseBase hotDramaList(@RequestBody(required = false) OpsDashboardHotDramaQuery query) {
		if (query == null) {
			query = new OpsDashboardHotDramaQuery();
		}
		OpsDashboardQuery rangeQuery = new OpsDashboardQuery();
		rangeQuery.setRangeType(query.getRangeType());
		rangeQuery.setStartDate(query.getStartDate());
		rangeQuery.setEndDate(query.getEndDate());
		LocalDate[] range;
		try {
			range = resolveRange(rangeQuery);
		} catch (IllegalArgumentException e) {
			return setResultError(e.getMessage());
		}
		LocalDate start = range[0];
		LocalDate end = range[1];
		String startDate = start.format(RankBoardGroupConstants.DATE_FMT);
		String endDate = end.format(RankBoardGroupConstants.DATE_FMT);
		LocalDate today = LocalDate.now(ZoneId.of(RankBoardGroupConstants.TIMEZONE));
		String todayStart = today.format(RankBoardGroupConstants.DATE_FMT) + " 00:00:00";
		String todayEndEx = today.plusDays(1).format(RankBoardGroupConstants.DATE_FMT) + " 00:00:00";

		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<OpsDashboardHotDramaItem> list = opsStatDao.selectHotDramaPage(
				startDate, endDate, todayStart, todayEndEx);
		if (list == null) {
			list = new ArrayList<>();
		}
		PageInfo<OpsDashboardHotDramaItem> page = new PageInfo<>(list);
		int baseRank = (query.getPageNumber() - 1) * query.getPageSize();
		int i = 0;
		for (OpsDashboardHotDramaItem item : list) {
			item.setRankNo(baseRank + (++i));
			long play = item.getPlayCount() == null ? 0L : item.getPlayCount();
			item.setPlayCountText(formatPlayWan(play));
			if (item.getTodayRecharge() == null) {
				item.setTodayRecharge(BigDecimal.ZERO);
			}
		}
		log.info("ops hotDramaList range={}~{} page={} size={} total={}",
				startDate, endDate, query.getPageNumber(), query.getPageSize(), page.getTotal());
		return setResultSuccess(page, I18nUtil.getMessage("base_success"));
	}

	/** KPI 卡片：始终今日口径 + 较昨日 */
	private void fillTodayKpi(OpsDashboardSummaryResp resp, LocalDate today,
			String todayStart, String todayEndEx, String ydayStart, String ydayEndEx) {
		BigDecimal todayRecharge = nvl(opsStatDao.sumCashRecharge(todayStart, todayEndEx));
		BigDecimal ydayRecharge = nvl(opsStatDao.sumCashRecharge(ydayStart, ydayEndEx));
		resp.setTodayRechargeAmount(todayRecharge);
		resp.setTodayRechargeMomRate(momRate(todayRecharge, ydayRecharge));

		long todayNew = opsStatDao.countNewUsers(todayStart, todayEndEx);
		long ydayNew = opsStatDao.countNewUsers(ydayStart, ydayEndEx);
		resp.setTodayNewUserCnt(todayNew);
		resp.setTodayNewUserMomRate(momRate(BigDecimal.valueOf(todayNew), BigDecimal.valueOf(ydayNew)));

		long paying = opsStatDao.countPayingUsers(todayStart, todayEndEx);
		resp.setPayingUserCnt(paying);
		String todayDate = today.format(RankBoardGroupConstants.DATE_FMT);
		long dau = userActiveDailyDao.countByDate(todayDate);
		if (dau <= 0) {
			// DAU 未落日表时用新增兜底，避免转化率除零无意义
			dau = todayNew;
		}
		if (dau > 0) {
			resp.setPayConversionRate(BigDecimal.valueOf(paying)
					.multiply(BigDecimal.valueOf(100))
					.divide(BigDecimal.valueOf(dau), 1, RoundingMode.HALF_UP));
		} else {
			resp.setPayConversionRate(BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP));
		}

		resp.setOnShelfDramaCnt(opsStatDao.countOnShelfDrama());
		resp.setPendingAuditDramaCnt(opsStatDao.countPendingAuditDrama());
	}

	private List<OpsDashboardTrendPoint> buildTrend(LocalDate start, LocalDate end,
			String startTime, String endExclusive) {
		Map<String, BigDecimal> rechargeMap = new HashMap<>();
		List<Map<String, Object>> rechargeRows = opsStatDao.sumCashRechargeByDay(startTime, endExclusive);
		if (rechargeRows != null) {
			for (Map<String, Object> row : rechargeRows) {
				String day = strVal(row, "bizDate");
				if (day != null) {
					rechargeMap.put(day, toDecimal(mapGet(row, "amount")));
				}
			}
		}
		Map<String, Long> playMap = new HashMap<>();
		String startDate = start.format(RankBoardGroupConstants.DATE_FMT);
		String endDate = end.format(RankBoardGroupConstants.DATE_FMT);
		List<Map<String, Object>> playRows = opsStatDao.sumPlayPvByDay(startDate, endDate);
		if (playRows != null) {
			for (Map<String, Object> row : playRows) {
				String day = strVal(row, "bizDate");
				if (day != null) {
					playMap.put(day, toLong(mapGet(row, "playCount")));
				}
			}
		}
		List<OpsDashboardTrendPoint> list = new ArrayList<>();
		for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
			String biz = d.format(RankBoardGroupConstants.DATE_FMT);
			OpsDashboardTrendPoint p = new OpsDashboardTrendPoint();
			p.setBizDate(biz);
			p.setLabel(d.format(LABEL_FMT));
			p.setRechargeAmount(rechargeMap.getOrDefault(biz, BigDecimal.ZERO));
			long play = playMap.getOrDefault(biz, 0L);
			p.setPlayCount(play);
			p.setPlayCountWan(BigDecimal.valueOf(play).divide(WAN, 1, RoundingMode.HALF_UP));
			list.add(p);
		}
		return list;
	}

	private List<OpsDashboardRevenuePart> buildRevenueParts(String startTime, String endExclusive) {
		BigDecimal episode = nvl(opsStatDao.sumEpisodeCash(startTime, endExclusive));
		BigDecimal member = nvl(opsStatDao.sumMemberCash(startTime, endExclusive));
		BigDecimal coin = nvl(opsStatDao.sumCoinSpendYuan(startTime, endExclusive));
		// 广告分成暂无落库，固定 0
		BigDecimal ad = BigDecimal.ZERO;
		BigDecimal total = episode.add(member).add(coin).add(ad);

		List<OpsDashboardRevenuePart> parts = new ArrayList<>();
		parts.add(part("EPISODE", "单集付费", episode, total));
		parts.add(part("MEMBER", "会员订阅", member, total));
		parts.add(part("COIN", "金币消费", coin, total));
		parts.add(part("AD", "广告分成", ad, total));
		return parts;
	}

	private OpsDashboardRevenuePart part(String type, String name, BigDecimal amount, BigDecimal total) {
		OpsDashboardRevenuePart p = new OpsDashboardRevenuePart();
		p.setType(type);
		p.setName(name);
		p.setAmount(amount);
		if (total.compareTo(BigDecimal.ZERO) <= 0) {
			p.setRate(BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP));
		} else {
			p.setRate(amount.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP));
		}
		return p;
	}

	private List<OpsDashboardHotDramaItem> buildHotTop5(String startDate, String endDate,
			String todayStart, String todayEndEx) {
		List<OpsDashboardHotDramaItem> list = opsStatDao.selectHotDramaTop(
				startDate, endDate, todayStart, todayEndEx, 5);
		if (list == null) {
			list = new ArrayList<>();
		}
		int rank = 1;
		for (OpsDashboardHotDramaItem item : list) {
			item.setRankNo(rank++);
			long play = item.getPlayCount() == null ? 0L : item.getPlayCount();
			item.setPlayCountText(formatPlayWan(play));
			if (item.getTodayRecharge() == null) {
				item.setTodayRecharge(BigDecimal.ZERO);
			}
		}
		return list;
	}

	private List<OpsDashboardTodoItem> buildTodos(Long pendingAudit) {
		List<OpsDashboardTodoItem> todos = new ArrayList<>();
		long audit = pendingAudit == null ? 0L : pendingAudit;
		todos.add(todo("AUDIT", "待审核内容",
				audit + " 部短剧等待审核", (int) audit, "/dramas/list"));

		long settleCnt = opsStatDao.countPendingSettleOrders();
		BigDecimal settleAmt = nvl(opsStatDao.sumPendingSettleAmount());
		todos.add(todo("SETTLE", "待结算收益",
				settleCnt + " 笔已支付待结算 ¥ " + settleAmt.setScale(0, RoundingMode.HALF_UP).toPlainString(),
				(int) Math.min(settleCnt, Integer.MAX_VALUE), "/order-revenue/recharge-order"));

		long appeal = opsStatDao.countPendingRefundApply();
		todos.add(todo("APPEAL", "用户申诉",
				appeal + " 条充值到账申诉待处理", (int) appeal, "/order-revenue/refund-manage"));
		return todos;
	}

	private static OpsDashboardTodoItem todo(String type, String title, String desc, int count, String link) {
		OpsDashboardTodoItem t = new OpsDashboardTodoItem();
		t.setType(type);
		t.setTitle(title);
		t.setDesc(desc);
		t.setCount(count);
		t.setLink(link);
		return t;
	}

	private static String formatPlayWan(long play) {
		if (play < 10000) {
			return play + " 次";
		}
		return BigDecimal.valueOf(play).divide(WAN, 1, RoundingMode.HALF_UP).toPlainString() + " 万";
	}

	private static BigDecimal momRate(BigDecimal current, BigDecimal previous) {
		if (previous == null || previous.compareTo(BigDecimal.ZERO) <= 0) {
			return null;
		}
		return current.subtract(previous)
				.multiply(BigDecimal.valueOf(100))
				.divide(previous, 1, RoundingMode.HALF_UP);
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

	/** 默认近 7 日，对齐总览原型 */
	private static String normalizeRangeType(String rangeType) {
		if (StringUtils.isEmpty(rangeType)) {
			return OpsDashboardConstants.RANGE_7D;
		}
		return rangeType.trim().toLowerCase();
	}

	private static BigDecimal nvl(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v;
	}

	private static Object mapGet(Map<String, Object> map, String key) {
		Object val = map.get(key);
		if (val == null) {
			val = map.get(key.toLowerCase());
		}
		if (val == null) {
			val = map.get(key.toUpperCase());
		}
		return val;
	}

	private static String strVal(Map<String, Object> map, String key) {
		Object val = mapGet(map, key);
		return val == null ? null : String.valueOf(val);
	}

	private static BigDecimal toDecimal(Object val) {
		if (val == null) {
			return BigDecimal.ZERO;
		}
		if (val instanceof BigDecimal) {
			return (BigDecimal) val;
		}
		return new BigDecimal(String.valueOf(val));
	}

	private static long toLong(Object val) {
		if (val == null) {
			return 0L;
		}
		if (val instanceof Number) {
			return ((Number) val).longValue();
		}
		try {
			return Long.parseLong(String.valueOf(val));
		} catch (NumberFormatException e) {
			return 0L;
		}
	}
}
