package com.playlet.internal.enums;

/**
 * 剧本授权类型：1 独家授权、2 普通授权、3 可改编。
 */
public enum ScriptLicenseTypeEnums {
	EXCLUSIVE(1, "独家授权"),
	NORMAL(2, "普通授权"),
	ADAPTABLE(3, "可改编");

	private final int code;
	private final String label;

	ScriptLicenseTypeEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static String getLabel(Integer code) {
		if (code == null) {
			return null;
		}
		for (ScriptLicenseTypeEnums item : values()) {
			if (item.code == code) {
				return item.label;
			}
		}
		return null;
	}
}
