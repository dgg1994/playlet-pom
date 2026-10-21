package com.playlet.internal.enums;

import lombok.Getter;

/**
 * 优惠券类型。
 */
@Getter
public enum CouponTypeEnums {

	PERCENT("PERCENT", "折扣"),
	FULL_REDUCE("FULL_REDUCE", "满减"),
	FIXED("FIXED", "立减");

	private final String code;
	private final String label;

	CouponTypeEnums(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public static CouponTypeEnums ofCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null;
		}
		String c = code.trim();
		for (CouponTypeEnums e : values()) {
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
