package com.playlet.internal.constants;

/**
 * 看剧喜好相关常量。
 */
public final class WatchPreferConstants {

	private WatchPreferConstants() {
	}

	/** 启用 */
	public static final int STATUS_ENABLED = 1;

	/** 停用 */
	public static final int STATUS_DISABLED = 0;

	/** max_select=0 表示不限制可选个数 */
	public static final int MAX_SELECT_UNLIMITED = 0;

	/** 首页「猜你喜欢」区块 groupId */
	public static final String HOME_PREFER_BLOCK_GROUP_ID = "rb_watch_prefer";

	/** 首页「猜你喜欢」展示名 */
	public static final String HOME_PREFER_BLOCK_NAME = "猜你喜欢";

	/** 首页猜你喜欢条数 */
	public static final int HOME_PREFER_LIMIT = 10;
}
