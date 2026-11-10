package com.playlet.internal.enums;

/**
 * 创作者身份资质审核状态。
 */
public enum CreatorIdentityAuditStatusEnums {
	PENDING(0, "未认证"),
	REVIEWING(1, "审核中"),
	PASS(2, "已认证"),
	REJECT(3, "驳回");

	private final int code;
	private final String label;

	CreatorIdentityAuditStatusEnums(int code, String label) {
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
