package com.playlet.internal.enums;

/**
 *  drama asset 审核状态
 */
public enum DramaAssetAuditStepStatusEnums {
	PENDING(0, "待审核"),
	PASS(1, "通过"),
	REJECT(2, "驳回");

	private final int code;
	private final String label;

	DramaAssetAuditStepStatusEnums(int code, String label) {
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
