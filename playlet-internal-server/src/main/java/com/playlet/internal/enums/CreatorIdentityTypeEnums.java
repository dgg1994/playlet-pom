package com.playlet.internal.enums;

/**
 * 作家身份类型。
 */
public enum CreatorIdentityTypeEnums {
	PERSONAL(1, "个人创作者"),
	ORG(2, "公司机构"),
	STUDIO(3, "工作室团队");

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
		return code.equals(PERSONAL.code) || code.equals(ORG.code) || code.equals(STUDIO.code);
	}

	public static CreatorIdentityTypeEnums fromCode(int code) {
		for (CreatorIdentityTypeEnums item : values()) {
			if (item.code == code) {
				return item;
			}
		}
		return null;
	}
}
