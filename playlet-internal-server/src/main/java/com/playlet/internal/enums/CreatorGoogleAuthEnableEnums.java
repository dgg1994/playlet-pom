package com.playlet.internal.enums;

/**
 * 作家端账号谷歌认证开关。
 */
public enum CreatorGoogleAuthEnableEnums {
	OFF(0, "关"),
	ON(1, "开");

	private final Integer code;
	private final String label;

	CreatorGoogleAuthEnableEnums(Integer code, String label) {
		this.code = code;
		this.label = label;
	}

	public Integer getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static boolean isValid(Integer code) {
		return OFF.code.equals(code) || ON.code.equals(code);
	}

	/** 空值按开启处理，与表默认 1 对齐 */
	public static boolean isOn(Integer code) {
		return code == null || ON.code.equals(code);
	}
}
