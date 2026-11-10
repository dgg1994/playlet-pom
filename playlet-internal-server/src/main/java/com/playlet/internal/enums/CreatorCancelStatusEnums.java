package com.playlet.internal.enums;

/**
 * 注销申请状态。
 */
public enum CreatorCancelStatusEnums {
	COOL_DOWN(1, "冷静期"),
	CANCELLED(2, "已注销"),
	REVOKED(3, "已撤销"),
	REJECTED(4, "已驳回");

	private final int code;
	private final String label;

	CreatorCancelStatusEnums(int code, String label) {
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
