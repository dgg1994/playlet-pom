package com.playlet.internal.constants;

/**
 * 优惠券业务常量。
 */
public final class CouponConstants {

	private CouponConstants() {
	}

	/** 模板启用 */
	public static final int TEMPLATE_STATUS_ON = 1;
	/** 模板停用 */
	public static final int TEMPLATE_STATUS_OFF = 0;

	/** 发放批次号前缀 */
	public static final String BATCH_NO_PREFIX = "CP";

	/** 默认每用户限领 */
	public static final int DEFAULT_PER_USER_LIMIT = 1;
}
