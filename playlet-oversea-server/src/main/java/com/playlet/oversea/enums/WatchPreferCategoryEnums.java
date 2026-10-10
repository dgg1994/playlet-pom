package com.playlet.oversea.enums;

/**
 * 看剧喜好分类编码（对齐 dic_watch_prefer_category.category_code）。
 */
public enum WatchPreferCategoryEnums {

	CHANNEL("CHANNEL", "频道"),
	ERA("ERA", "时代背景"),
	STYLE("STYLE", "风格"),
	THEME("THEME", "主题情节"),
	ROLE("ROLE", "角色");

	private final String code;
	private final String name;

	WatchPreferCategoryEnums(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	/**
	 * 按编码解析；未知编码返回 null。
	 */
	public static WatchPreferCategoryEnums ofCode(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}
		for (WatchPreferCategoryEnums e : values()) {
			if (e.code.equals(code)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 是否合法分类编码。
	 */
	public static boolean isValid(String code) {
		return ofCode(code) != null;
	}
}
