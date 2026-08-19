package com.playlet.internal.enums;

/**
 * 审核组类型枚举
 */
public enum DramaAssetAuditStepTypeEnums {
	AI(1, "AI审核"),
	GROUP_A(2, "A组审核"),
	GROUP_B(3, "B组审核");

	private final int code;
	private final String label;

	DramaAssetAuditStepTypeEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static DramaAssetAuditStepTypeEnums fromCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (DramaAssetAuditStepTypeEnums item : values()) {
			if (item.code == code) {
				return item;
			}
		}
		return null;
	}

	/** 是否人工审核组（A / B），AI 不算。 */
	public static boolean isManualGroup(Integer code) {
		DramaAssetAuditStepTypeEnums item = fromCode(code);
		return item == GROUP_A || item == GROUP_B;
	}
}
