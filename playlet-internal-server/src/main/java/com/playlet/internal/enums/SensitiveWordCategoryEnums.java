package com.playlet.internal.enums;

import lombok.Getter;

/**
 * 敏感词分类（枚举维护，不建分类表）。
 */
@Getter
public enum SensitiveWordCategoryEnums {

	FORBIDDEN("FORBIDDEN", "违禁内容"),
	AD("AD", "广告导流");

	private final String code;
	private final String label;

	SensitiveWordCategoryEnums(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public static SensitiveWordCategoryEnums ofCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null;
		}
		String c = code.trim();
		for (SensitiveWordCategoryEnums e : values()) {
			if (e.code.equalsIgnoreCase(c)) {
				return e;
			}
		}
		return null;
	}

	public static boolean isValid(String code) {
		return ofCode(code) != null;
	}
}
