package com.playlet.oversea.constants;

/**
 * 签到业务常量。
 */
public final class SignInConstants {

	private SignInConstants() {
	}

	/** 开关：关 */
	public static final int SWITCH_OFF = 0;
	/** 开关：开 */
	public static final int SWITCH_ON = 1;

	/** 签到模式：新手 */
	public static final String MODE_NEWBIE = "NEWBIE";
	/** 签到模式：日常连续 */
	public static final String MODE_DAILY = "DAILY";

	/** 新手结束：满次数 */
	public static final int NEWBIE_END_FULL = 1;
	/** 新手结束：窗口过期 */
	public static final int NEWBIE_END_EXPIRED = 2;

	/** 默认新手窗口天数 */
	public static final int DEFAULT_NEWBIE_WINDOW_DAYS = 14;
	/** 默认新手最多签到次数 */
	public static final int DEFAULT_NEWBIE_MAX_TIMES = 7;
}
