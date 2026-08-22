package com.playlet.oversea.enums;

/**
 * OnePay 绑定状态。
 */
public enum OnePayBindStatusEnums {
	UNBOUND(0, "未绑定"),
	BOUND(1, "已绑定");

	private final int code;
	private final String label;

	OnePayBindStatusEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}
}
