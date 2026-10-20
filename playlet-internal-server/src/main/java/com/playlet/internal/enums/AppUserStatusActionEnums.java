package com.playlet.internal.enums;

/**
 * C 端用户账号状态流水动作。
 */
public enum AppUserStatusActionEnums {

	REGISTER("REGISTER", "正常"),
	MUTE("MUTE", "临时禁言"),
	UNMUTE("UNMUTE", "解除禁言"),
	FREEZE("FREEZE", "已封"),
	UNFREEZE("UNFREEZE", "正常"),
	NORMAL("NORMAL", "正常");

	private final String code;
	private final String label;

	AppUserStatusActionEnums(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static String labelOf(String code) {
		if (code == null) {
			return null;
		}
		for (AppUserStatusActionEnums e : values()) {
			if (e.code.equals(code)) {
				return e.label;
			}
		}
		return code;
	}
}
