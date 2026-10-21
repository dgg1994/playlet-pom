package com.playlet.internal.enums;

import lombok.Getter;

/**
 * 敏感词审核状态。
 */
@Getter
public enum SensitiveWordStatusEnums {

	PENDING(0, "待审核"),
	ACTIVE(1, "已生效"),
	REJECTED(2, "已驳回"),
	DISABLED(3, "已禁用");

	private final Integer index;
	private final String label;

	SensitiveWordStatusEnums(Integer index, String label) {
		this.index = index;
		this.label = label;
	}

	public static SensitiveWordStatusEnums ofIndex(Integer index) {
		if (index == null) {
			return null;
		}
		for (SensitiveWordStatusEnums e : values()) {
			if (e.index.equals(index)) {
				return e;
			}
		}
		return null;
	}

	public static boolean isValid(Integer index) {
		return ofIndex(index) != null;
	}
}
