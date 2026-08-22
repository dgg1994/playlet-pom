package com.playlet.internal.constants;

/**
 * 运营看板统计常量。
 */
public final class OpsDashboardConstants {

	private OpsDashboardConstants() {
	}

	/** 今日 */
	public static final String RANGE_TODAY = "today";
	/** 近 7 日（含今日） */
	public static final String RANGE_7D = "7d";
	/** 近 30 日（含今日） */
	public static final String RANGE_30D = "30d";
	/** 自定义起止（含首尾） */
	public static final String RANGE_CUSTOM = "custom";

	public static final int DAYS_7 = 7;
	public static final int DAYS_30 = 30;
	/** 自定义区间最大天数，防止扫表过重 */
	public static final int MAX_CUSTOM_DAYS = 90;
}
