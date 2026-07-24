package com.playlet.internal.constants;

/**
 * 算法榜 group_id 约定（与 rank_board.group_id 一致）
 */
public final class RankBoardGroupConstants {

	public static final String HOT_PLAY = "rb_hot_play";
	public static final String NEW = "rb_new";
	public static final String COLLECT = "rb_collect";

	public static final String TIMEZONE = "Asia/Shanghai";

	/** 热播/收藏统计窗口（天） */
	public static final int WINDOW_DAYS_HOT = 7;
	/** 新剧上架窗口（天） */
	public static final int WINDOW_DAYS_NEW = 14;

	private RankBoardGroupConstants() {
	}
}
