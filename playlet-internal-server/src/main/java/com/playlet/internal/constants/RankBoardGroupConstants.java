package com.playlet.internal.constants;

import java.time.format.DateTimeFormatter;

/**
 * 算法榜 group_id 约定（与 rank_board.group_id 一致）
 */
public final class RankBoardGroupConstants {

	public static final String HOT_PLAY = "rb_hot_play";
	public static final String NEW = "rb_new";
	public static final String RISING = "rb_rising";
	public static final String RECOMMEND = "rb_recommend";
	public static final String HOT_SEARCH = "rb_hot_search";
	public static final String COLLECT = "rb_collect";

	public static final String TIMEZONE = "Asia/Shanghai";

	/** 热播/收藏/热搜/推荐统计窗口（天） */
	public static final int WINDOW_DAYS_HOT = 7;
	/** 新剧上架窗口（天） */
	public static final int WINDOW_DAYS_NEW = 14;
	/** 飙升：近 N 天 vs 前 N 天 */
	public static final int WINDOW_DAYS_RISING = 3;
	/** 飙升：近窗有效观看秒数下限，过滤噪声 */
	public static final int RISING_MIN_VALID_SECONDS = 60;
	/** 热搜：单次标题搜索最多给前 K 条命中记 search_cnt */
	public static final int HOT_SEARCH_HIT_CAP = 20;
	/** 推荐榜：上架新鲜度加权窗口（天） */
	public static final int RECOMMEND_FRESH_DAYS = 30;
	/** 推荐榜：每「距今天数」递减的新鲜度加分（越新越高） */
	public static final int RECOMMEND_FRESH_WEIGHT_PER_DAY = 100;

	public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private RankBoardGroupConstants() {
	}
}
