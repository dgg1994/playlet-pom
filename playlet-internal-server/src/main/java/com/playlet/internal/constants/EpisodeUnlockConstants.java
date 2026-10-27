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

	/** 解锁结果：已解锁（金币/广告/会员直通） */
	public static final String UNLOCK_STATUS_UNLOCKED = "UNLOCKED";

	/** 解锁结果：待支付（现金下单成功，等待回调） */
	public static final String UNLOCK_STATUS_WAIT_PAY = "WAIT_PAY";

	/** 解锁范围：只解锁当前集 */
	public static final String UNLOCK_SCOPE_SINGLE = "SINGLE";

	/** 解锁范围：当前集及之后所有付费集（后续剧集全部解锁） */
	public static final String UNLOCK_SCOPE_REMAINING = "REMAINING";

	private EpisodeUnlockConstants() {
	}
}
