package com.playlet.internal.enums;

/**
 * 剧单标签匹配模式（drama_playlist.match_mode）。
 */
public enum PlaylistMatchModeEnums {

	OR(1, "命中任一标签"),
	AND(2, "须命中全部标签");

	private final Integer index;
	private final String name;

	PlaylistMatchModeEnums(Integer index, String name) {
		this.index = index;
		this.name = name;
	}

	public Integer getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public static boolean isAnd(Integer matchMode) {
		return AND.getIndex().equals(matchMode);
	}

	public static boolean isValid(Integer matchMode) {
		return OR.getIndex().equals(matchMode) || AND.getIndex().equals(matchMode);
	}
}
