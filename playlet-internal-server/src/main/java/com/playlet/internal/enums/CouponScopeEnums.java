package com.playlet.internal.enums;

import lombok.Getter;

/**
 * 优惠券适用范围。
 */
@Getter
public enum CouponScopeEnums {

	MEMBER("MEMBER", "购买会员"),
	COIN_CONSUME("COIN_CONSUME", "金币消费"),
	RECHARGE("RECHARGE", "充值"),
	ALL("ALL", "通用");

	private final String code;
	private final String label;

	CouponScopeEnums(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public static CouponScopeEnums ofCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null;
		}
		String c = code.trim();
		for (CouponScopeEnums e : values()) {
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
