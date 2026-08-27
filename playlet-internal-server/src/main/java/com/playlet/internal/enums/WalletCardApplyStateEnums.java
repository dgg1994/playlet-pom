package com.playlet.internal.enums;

/**
 * 开卡申请本地状态。
 */
public enum WalletCardApplyStateEnums {

	APPLYING(0, "申请中"),
	ISSUED(1, "已发卡"),
	REJECTED(2, "已拒绝");

	private final int code;
	private final String label;

	WalletCardApplyStateEnums(int code, String label) {
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
