package com.playlet.internal.constants;

/**
 * 极光推送相关常量
 */
public final class PushConstants {

	private PushConstants() {
	}

	/** extras.bizType：互动消息 */
	public static final String BIZ_INTERACT = "INTERACT";
	/** extras.bizType：勋章 */
	public static final String BIZ_MEDAL = "MEDAL";
	/** extras.bizType：系统消息 */
	public static final String BIZ_SYSTEM = "SYSTEM";

	/** 极光单次 registrationId / alias 上限 */
	public static final int JPUSH_REG_BATCH = 1000;
}
