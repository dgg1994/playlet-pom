package com.playlet.internal.enums;

import lombok.Getter;

/**
 * 充值订单商品类型（管理端统一列表）。
 */
@Getter
public enum RechargeProductTypeEnums {

	MEMBER("MEMBER", "会员"),
	EPISODE("EPISODE", "单集解锁");

	private final String code;
	private final String label;

	RechargeProductTypeEnums(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public static RechargeProductTypeEnums ofCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null;
		}
		String c = code.trim();
		for (RechargeProductTypeEnums e : values()) {
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
