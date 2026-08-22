package com.playlet.oversea.enums;

/**
 * 作家首页榜单类型。
 */
public enum CreatorHomeRankTypeEnums {
	INFLUENCE(1, "影响力榜"),
	GROWTH(2, "成长力榜");

	private final Integer code;
	private final String label;

	CreatorHomeRankTypeEnums(Integer code, String label) {
		this.code = code;
		this.label = label;
	}

	public Integer getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static CreatorHomeRankTypeEnums fromCode(Integer code) {
		if (code == null) {
			return INFLUENCE;
		}
		for (CreatorHomeRankTypeEnums item : values()) {
			if (item.code.equals(code)) {
				return item;
			}
		}
		return null;
	}
}
