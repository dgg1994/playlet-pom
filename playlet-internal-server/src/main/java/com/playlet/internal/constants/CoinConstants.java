package com.playlet.internal.constants;

/**
 * 金币经济常量。
 */
public final class CoinConstants {

	/** 汇率模式：手动 */
	public static final int RATE_MODE_MANUAL = 1;

	/** 汇率模式：按日广告公式 */
	public static final int RATE_MODE_DAILY = 2;

	/** 清币开关：开 */
	public static final int IDLE_CLEAR_ON = 1;

	/** 清币开关：关 */
	public static final int IDLE_CLEAR_OFF = 0;

	/** 清币一次扫描上限 */
	public static final int IDLE_CLEAR_BATCH = 200;

	/** 清币流水 biz_type */
	public static final String BIZ_IDLE_CLEAR = "SYSTEM";

	/** 清币流水 task_code */
	public static final String TASK_IDLE_CLEAR = "IDLE_CLEAR";

	private CoinConstants() {
	}
}
