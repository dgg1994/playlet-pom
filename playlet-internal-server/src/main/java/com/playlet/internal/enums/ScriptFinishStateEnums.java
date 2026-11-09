package com.playlet.internal.enums;

/**
 * 剧本完成状态：0 连载、1 完结。
 */
public enum ScriptFinishStateEnums {
	SERIAL(0, "连载中"),
	FINISHED(1, "已完结");

	private final int code;
	private final String label;

	ScriptFinishStateEnums(int code, String label) {
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
		for (ScriptFinishStateEnums item : values()) {
			if (item.code == code) {
				return item.label;
			}
		}
		return null;
	}
}
