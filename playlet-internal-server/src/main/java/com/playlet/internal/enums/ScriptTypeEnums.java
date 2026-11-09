package com.playlet.internal.enums;

/**
 * 剧本类型：1 真人短剧、2 AI 漫剧。
 */
public enum ScriptTypeEnums {
	REAL(1, "真人短剧"),
	AI_COMIC(2, "AI漫剧");

	private final int code;
	private final String label;

	ScriptTypeEnums(int code, String label) {
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
		for (ScriptTypeEnums item : values()) {
			if (item.code == code) {
				return item.label;
			}
		}
		return null;
	}
}
