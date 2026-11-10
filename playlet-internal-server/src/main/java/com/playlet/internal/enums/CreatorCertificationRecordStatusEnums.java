package com.playlet.internal.enums;

/**
 * 创作者认证审核记录状态（认证管理页时间线）。
 */
public enum CreatorCertificationRecordStatusEnums {
	REVIEWING(1, "审核中"),
	PASS(2, "已通过"),
	REJECT(3, "已驳回"),
	DONE(4, "已处理");

	private final int code;
	private final String label;

	CreatorCertificationRecordStatusEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static String labelOf(Integer code) {
		if (code == null) {
			return "";
		}
		for (CreatorCertificationRecordStatusEnums e : values()) {
			if (e.code == code) {
				return e.label;
			}
		}
		return "";
	}
}
