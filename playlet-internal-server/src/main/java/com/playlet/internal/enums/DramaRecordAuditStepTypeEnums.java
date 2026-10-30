package com.playlet.internal.enums;

/**
 * 备案审核步骤类型。
 */
public enum DramaRecordAuditStepTypeEnums {
	FIRST(1, "初审"),
	SECOND(2, "复审");

	private final int code;
	private final String label;

	DramaRecordAuditStepTypeEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static DramaRecordAuditStepTypeEnums fromCode(Integer code) {
		if (code == null) return null;
		for (DramaRecordAuditStepTypeEnums item : values()) {
			if (item.code == code) return item;
		}
		return null;
	}

	public static boolean isHandleAction(Integer code) {
		return fromCode(code) != null;
	}
}
