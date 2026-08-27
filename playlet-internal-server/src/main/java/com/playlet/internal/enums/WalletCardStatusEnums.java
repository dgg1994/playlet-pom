package com.playlet.internal.enums;

/**
 * worldPay 银行卡状态（对齐 /api/bankcard/user/card/list status）。
 */
public enum WalletCardStatusEnums {

	INITIAL(0, "初始状态"),
	WAIT_ACTIVE(1, "待激活"),
	ACTIVATING(2, "激活中"),
	ACTIVE(3, "正常"),
	FREEZE(4, "冻结"),
	LOST(5, "挂失"),
	PRE_CLOSE(6, "注销前"),
	CLOSED(7, "注销"),
	EXPIRED(8, "过期"),
	SUSPEND(9, "暂停");

	private final int code;
	private final String label;

	WalletCardStatusEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static String getLabelByCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (WalletCardStatusEnums e : values()) {
			if (e.code == code) {
				return e.label;
			}
		}
		return String.valueOf(code);
	}
}
