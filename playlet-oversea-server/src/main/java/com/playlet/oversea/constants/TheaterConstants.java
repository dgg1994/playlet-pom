package com.playlet.oversea.constants;

/**
 * 剧场相关常量（观看上报、首页预览等）
 */
public final class TheaterConstants {

	/** 单次上报有效观看秒数上限，防刷 */
	public static final int MAX_DELTA_SEC_PER_REPORT = 300;

	/** 未传单集时长时的默认值（秒） */
	public static final int DEFAULT_EPISODE_DURATION_SEC = 90;

	/** 单集时长下限（秒） */
	public static final int MIN_EPISODE_DURATION_SEC = 10;

	/** 单集时长上限（秒） */
	public static final int MAX_EPISODE_DURATION_SEC = 3600;

	/**
	 * 热度换算分母系数：每有效观看「单集时长 / HOT_SCORE_DURATION_DIVISOR」+1。
	 * 公式：add = floor(deltaSec * HOT_SCORE_DURATION_DIVISOR / episodeDurationSec)
	 */
	public static final int HOT_SCORE_DURATION_DIVISOR = 3;

	/** 首页各榜预览条数上限 */
	public static final int HOME_RANK_PREVIEW_MAX = 10;

	/** 首页推荐轮播条数上限 */
	public static final int HOME_CAROUSEL_LIMIT = 5;

	/**
	 * 完播判定：观看进度占单集时长的百分比阈值（含）。
	 * 例：90 表示 progress >= duration * 90%
	 */
	public static final int COMPLETE_PROGRESS_PERCENT = 90;

	/** 同一用户同一集：曝光去重 TTL（秒），按自然日近似用 1 天 */
	public static final long PLAY_EXPOSE_DEDUP_TTL_SEC = 24L * 60 * 60;

	/** 同一用户同一集：完播去重 TTL（秒） */
	public static final long PLAY_COMPLETE_DEDUP_TTL_SEC = 90L * 24 * 60 * 60;

	private TheaterConstants() {
	}
}
