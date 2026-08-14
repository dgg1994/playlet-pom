package com.playlet.internal.enums;

/**
 * 作家入驻审核状态。
 */
public enum CreatorProfileAuditStatusEnums {
	PENDING(0, "待审"),
	UNDER_REVIEW(1, "审核中"),
	APPROVED(2, "通过"),
	REJECTED(3, "驳回");

	private final int code;
	private final String label;

	CreatorProfileAuditStatusEnums(int code, String label) {
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
