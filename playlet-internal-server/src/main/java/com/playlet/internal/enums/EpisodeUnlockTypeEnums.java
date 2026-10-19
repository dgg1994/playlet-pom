package com.playlet.internal.enums;

/**
 * 剧集解锁类型（drama_asset.unlock_type）。
 */
public enum EpisodeUnlockTypeEnums {

	FREE(0, "免费"),
	AD(1, "广告解锁"),
	PAY(2, "付费解锁");

	private final Integer index;
	private final String name;

	EpisodeUnlockTypeEnums(Integer index, String name) {
		this.index = index;
		this.name = name;
	}

	public Integer getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public static boolean isFree(Integer unlockType) {
		return FREE.getIndex().equals(unlockType);
	}

	public static boolean isAd(Integer unlockType) {
		return AD.getIndex().equals(unlockType);
	}

	public static boolean isPay(Integer unlockType) {
		return PAY.getIndex().equals(unlockType);
	}
}
