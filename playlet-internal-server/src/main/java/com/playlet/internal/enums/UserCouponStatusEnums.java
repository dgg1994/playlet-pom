package com.playlet.internal.enums;

import lombok.Getter;

/**
 * 用户持券状态。
 */
@Getter
public enum UserCouponStatusEnums {

	UNUSED(0, "未用"),
	USED(1, "已用"),
	EXPIRED(2, "过期"),
	VOIDED(3, "作废");

	private final Integer index;
	private final String label;

	UserCouponStatusEnums(Integer index, String label) {
		this.index = index;
		this.label = label;
	}

	public static UserCouponStatusEnums ofIndex(Integer index) {
		if (index == null) {
			return null;
		}
		for (UserCouponStatusEnums e : values()) {
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
