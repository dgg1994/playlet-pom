package com.playlet.internal.enums;

/**
 *  drama asset 审核状态
 */
public enum DramaAssetAuditStatusEnums {
	PENDING(0, "待审核"),
	UNDER_REVIEW(1, "审核中"),
	APPROVED(2, "审核通过"),
	REJECTED(3, "审核驳回");

	private final int code;
	private final String label;

	DramaAssetAuditStatusEnums(int code, String label) {
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
