package com.playlet.internal.enums;

/**
 * 作家身份类型。
 */
public enum CreatorIdentityTypeEnums {
	PERSONAL(1, "个人创作者"),
	ORG(2, "创作机构");

	private final int code;
	private final String label;

	CreatorIdentityTypeEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static boolean isValid(Integer code) {
		if (code == null) {
			return false;
		}
		return code.equals(PERSONAL.code) || code.equals(ORG.code);
	}
}
