package com.playlet.internal.enums;

import lombok.Getter;

/**
 * 优惠券发放方式。
 */
@Getter
public enum CouponIssueTypeEnums {

	MANUAL("MANUAL", "手动发放"),
	EVENT("EVENT", "事件自动发放");

	private final String code;
	private final String label;

	CouponIssueTypeEnums(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public static CouponIssueTypeEnums ofCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null;
		}
		String c = code.trim();
		for (CouponIssueTypeEnums e : values()) {
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
