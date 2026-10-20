package com.playlet.internal.constants;

/**
 * 剧集解锁 / 播放策略常量。
 */
public final class EpisodeUnlockConstants {

	/** 激励广告场景：解锁剧集 */
	public static final String AD_SCENE_UNLOCK = "UNLOCK";

	/** 广告位业务码默认：激励解锁 */
	public static final String AD_SLOT_REWARD_UNLOCK = "REWARD_UNLOCK";

	/** 金币解锁流水 task_code */
	public static final String COIN_TASK_EPISODE_UNLOCK = "EPISODE_UNLOCK";

	/** 会员短剧标记：是 */
	public static final int MEMBER_DRAMA_YES = 1;

	/** 播放拦截：需登录 */
	public static final String BLOCK_NEED_LOGIN = "NEED_LOGIN";

	/** 播放拦截：需开通会员 */
	public static final String BLOCK_NEED_MEMBER = "NEED_MEMBER";

	/** 播放拦截：需看广告 */
	public static final String BLOCK_NEED_AD = "NEED_AD";

	/** 播放拦截：需付费（金币/现金） */
	public static final String BLOCK_NEED_PAY = "NEED_PAY";

	/** 播放拦截：未上架 */
	public static final String BLOCK_NOT_ON_SHELF = "NOT_ON_SHELF";

	private EpisodeUnlockConstants() {
	}
}
