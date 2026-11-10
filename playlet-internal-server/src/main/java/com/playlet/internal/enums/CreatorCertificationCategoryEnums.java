package com.playlet.internal.enums;

/**
 * 创作者认证类别（认证管理页双 Tab）。
 */
public enum CreatorCertificationCategoryEnums {
	PERSONAL(1, "个人创作者"),
	ORG(2, "机构创作者");

	private final int code;
	private final String label;

	CreatorCertificationCategoryEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static CreatorCertificationCategoryEnums fromCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (CreatorCertificationCategoryEnums e : values()) {
			if (e.code == code) {
				return e;
			}
		}
		return null;
	}
}
