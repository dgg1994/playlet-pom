package com.playlet.internal.enums;

import lombok.Getter;

/**
 * 优惠券面额单位。
 */
@Getter
public enum CouponCurrencyEnums {

	YUAN("YUAN", "元"),
	COIN("COIN", "金币");

	private final String code;
	private final String label;

	CouponCurrencyEnums(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public static CouponCurrencyEnums ofCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null;
		}
		String c = code.trim();
		for (CouponCurrencyEnums e : values()) {
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
