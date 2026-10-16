package com.playlet.internal.enums;

/**
 * 剧场首页 Tab：热榜 / 剧单 / 推荐。
 */
public enum TheaterHomeTabEnums {

	HOT("hot", "热榜"),
	PLAYLIST("playlist", "剧单"),
	RECOMMEND("recommend", "推荐");

	private final String code;
	private final String name;

	TheaterHomeTabEnums(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	/** 空则默认推荐；无法识别返回 null */
	public static TheaterHomeTabEnums ofCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return RECOMMEND;
		}
		String trimmed = code.trim();
		for (TheaterHomeTabEnums item : values()) {
			if (item.code.equalsIgnoreCase(trimmed)) {
				return item;
			}
		}
		return null;
	}
}
