package com.playlet.internal.enums;

/**
 * 用户实际解锁方式（user_episode_unlock.unlock_way）。
 */
public enum EpisodeUnlockWayEnums {

	FREE("FREE", "免费"),
	AD("AD", "广告"),
	COIN("COIN", "金币"),
	CASH("CASH", "现金"),
	MEMBER("MEMBER", "会员直通");

	private final String code;
	private final String name;

	EpisodeUnlockWayEnums(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}
