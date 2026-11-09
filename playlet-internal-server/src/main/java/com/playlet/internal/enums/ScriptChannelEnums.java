package com.playlet.internal.enums;

/**
 * 剧本内容分区：1 女频、2 男频、3 合家欢。
 */
public enum ScriptChannelEnums {
	FEMALE(1, "女频"),
	MALE(2, "男频"),
	FAMILY(3, "合家欢");

	private final int code;
	private final String label;

	ScriptChannelEnums(int code, String label) {
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
		for (ScriptChannelEnums item : values()) {
			if (item.code == code) {
				return item.label;
			}
		}
		return null;
	}
}
