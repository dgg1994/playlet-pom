package com.playlet.internal.constants;

/**
 * 作家端账号常量。
 */
public final class CreatorConstants {

	private CreatorConstants() {
	}

	/** JWT subject 前缀，避免与 C 端 / 运营账号 Redis 会话撞车 */
	public static final String JWT_SUBJECT_PREFIX = "creator:";

	/** 展示昵称最大字数（原型：不超过九个字；注册自动生成不受此限） */
	public static final int NICKNAME_MAX_LEN = 9;

	/** 注册默认昵称前缀，与 C 端 AppUserServiceImpl 一致 */
	public static final String NICKNAME_AUTO_PREFIX = "creator_";

	/** 注册默认昵称时间戳格式 */
	public static final String NICKNAME_AUTO_TIME_PATTERN = "yyMMddHHmmss";

	/** 注册默认昵称随机数区间（含 origin，不含 bound，同 ThreadLocalRandom.nextInt） */
	public static final int NICKNAME_AUTO_RANDOM_ORIGIN = 100;
	public static final int NICKNAME_AUTO_RANDOM_BOUND = 999;

	/** 登录邮箱格式 */
	public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
}
