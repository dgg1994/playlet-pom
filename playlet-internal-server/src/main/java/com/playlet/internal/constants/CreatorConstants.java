package com.playlet.internal.constants;

/**
 * 作家端账号常量。
 */
public final class CreatorConstants {

	private CreatorConstants() {
	}

	/** JWT subject 前缀，避免与 C 端 / 运营账号 Redis 会话撞车 */
	public static final String JWT_SUBJECT_PREFIX = "creator:";

	/** 展示昵称最大字数（原型：不超过九个字） */
	public static final int NICKNAME_MAX_LEN = 9;

	/** 登录邮箱格式 */
	public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
}
