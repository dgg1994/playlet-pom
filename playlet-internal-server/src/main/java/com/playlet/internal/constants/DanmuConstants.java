package com.playlet.internal.constants;

/**
 * 弹幕相关常量。
 */
public final class DanmuConstants {

	private DanmuConstants() {
	}

	/** 正文最大长度 */
	public static final int CONTENT_MAX_LEN = 100;

	/** 默认颜色 */
	public static final String DEFAULT_COLOR = "#FFFFFF";

	/** 字体：标准 */
	public static final int FONT_SIZE_NORMAL = 1;

	/** 字体：加大 */
	public static final int FONT_SIZE_LARGE = 2;

	/** 单次时间窗拉取上限 */
	public static final int LIST_WINDOW_LIMIT = 500;

	/** 单次查询允许的最大时间窗（秒） */
	public static final int LIST_MAX_WINDOW_SEC = 120;

	/** 同一用户同一集每分钟最多发送条数 */
	public static final int SEND_RATE_LIMIT_PER_MINUTE = 20;
}
