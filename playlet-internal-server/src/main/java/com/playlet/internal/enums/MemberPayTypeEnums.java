package com.playlet.internal.enums;

/**
 * 会员支付类型：金币 / 现金。
 */
public enum MemberPayTypeEnums {

	COIN("COIN", "金币"),
	CASH("CASH", "现金");

	private final String code;
	private final String name;

	MemberPayTypeEnums(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static MemberPayTypeEnums ofCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null;
		}
		String trimmed = code.trim();
		for (MemberPayTypeEnums item : values()) {
			if (item.code.equalsIgnoreCase(trimmed)) {
				return item;
			}
		}
		return null;
	}

	public static boolean isCoin(String code) {
		return COIN == ofCode(code);
	}

	public static boolean isCash(String code) {
		return CASH == ofCode(code);
	}
}
