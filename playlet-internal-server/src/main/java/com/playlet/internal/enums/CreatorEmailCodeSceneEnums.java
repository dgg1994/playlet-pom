package com.playlet.internal.enums;

/**
 * 作家端邮箱验证码场景。
 */
public enum CreatorEmailCodeSceneEnums {
	REGISTER(1, "注册"),
	RESET_PWD(2, "找回密码"),
	BIND_ONEPAY(3, "绑定/解绑OnePay");

	private final int code;
	private final String label;

	CreatorEmailCodeSceneEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static CreatorEmailCodeSceneEnums fromCode(Integer code) {
		if (code == null) {
			return REGISTER;
		}
		for (CreatorEmailCodeSceneEnums item : values()) {
			if (item.code == code) {
				return item;
			}
		}
		return null;
	}
}
