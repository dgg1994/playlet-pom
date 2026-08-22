package com.playlet.oversea.enums;

/**
 * 剧/集申诉状态（字段方案，非独立表）。
 */
public enum DramaAppealStatusEnums {
	NONE(0, "无申诉"),
	APPEALING(1, "申诉中"),
	APPEAL_PASS(2, "申诉通过"),
	APPEAL_REJECT(3, "申诉驳回");

	private final int code;
	private final String label;

	DramaAppealStatusEnums(int code, String label) {
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
