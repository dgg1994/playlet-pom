package com.playlet.internal.enums;

/**
 * 剧本上架状态：0 下架、1 上架。
 */
public enum ScriptShelfStatusEnums {
	OFF(0, "已下架"),
	ON(1, "已上架");

	private final int code;
	private final String label;

	ScriptShelfStatusEnums(int code, String label) {
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
		for (ScriptShelfStatusEnums item : values()) {
			if (item.code == code) {
				return item.label;
			}
		}
		return null;
	}

	public static ScriptShelfStatusEnums fromCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (ScriptShelfStatusEnums item : values()) {
			if (item.code == code) {
				return item;
			}
		}
		return null;
	}
}
