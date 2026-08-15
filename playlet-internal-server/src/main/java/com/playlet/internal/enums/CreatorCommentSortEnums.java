package com.playlet.internal.enums;

/**
 * 作家端评论列表排序。
 */
public enum CreatorCommentSortEnums {
	HEAT(1, "按热度"),
	TIME(2, "按时间");

	private final Integer code;
	private final String label;

	CreatorCommentSortEnums(Integer code, String label) {
		this.code = code;
		this.label = label;
	}

	public Integer getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static CreatorCommentSortEnums fromCode(Integer code) {
		if (code == null) {
			return TIME;
		}
		for (CreatorCommentSortEnums item : values()) {
			if (item.code.equals(code)) {
				return item;
			}
		}
		return null;
	}
}
