package com.playlet.internal.enums;

/**
 * 短剧备案分类（广电备案 / 平台备案）。
 */
public enum DramaFilingCategoryEnums {
	/** 一类：重点网络剧（广电备案） */
	FIRST(1, "一类"),
	/** 二类：微短剧（广电备案） */
	SECOND(2, "二类"),
	/** 三类：平台短剧（平台备案） */
	THIRD(3, "三类");

	private final int code;
	private final String label;

	DramaFilingCategoryEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static DramaFilingCategoryEnums fromCode(Integer code) {
		if (code == null) return null;
		for (DramaFilingCategoryEnums item : values()) {
			if (item.code == code) return item;
		}
		return null;
	}
}
