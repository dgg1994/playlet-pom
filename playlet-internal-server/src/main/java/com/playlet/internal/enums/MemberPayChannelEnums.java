package com.playlet.internal.enums;

/**
 * 现金支付渠道（payType=CASH 时使用）。
 */
public enum MemberPayChannelEnums {

	WECHAT("WECHAT", "微信"),
	ALIPAY("ALIPAY", "支付宝"),
	BANK("BANK", "银行卡");

	private final String code;
	private final String name;

	MemberPayChannelEnums(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static MemberPayChannelEnums ofCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null;
		}
		String trimmed = code.trim();
		for (MemberPayChannelEnums item : values()) {
			if (item.code.equalsIgnoreCase(trimmed)) {
				return item;
			}
		}
		return null;
	}

	public static boolean isValid(String code) {
		return ofCode(code) != null;
	}
}
