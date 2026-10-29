package com.playlet.internal.constants;

/**
 * 会员业务常量。
 */
public final class MemberConstants {

	/** 会员权益有效 */
	public static final int MEMBER_STATUS_ON = 1;

	/** 会员权益关闭 */
	public static final int MEMBER_STATUS_OFF = 0;

	/** 套餐上架 */
	public static final int PRODUCT_STATUS_ON = 1;

	/** 金币买会员流水 task_code */
	public static final String COIN_TASK_MEMBER_BUY = "MEMBER_BUY";

	/** 自动续费开关：开启 */
	public static final int AUTO_RENEW_ON = 1;

	/** 自动续费开关：关闭 */
	public static final int AUTO_RENEW_OFF = 0;

	private MemberConstants() {
	}
}
