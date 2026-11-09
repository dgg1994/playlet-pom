package com.playlet.internal.enums;

/**
 * 购买/议价申请类型：1 购买、2 议价。
 */
public enum ScriptTradeApplyTypeEnums {
	BUY(1, "购买"),
	NEGOTIATE(2, "议价");

	private final int code;
	private final String label;

	ScriptTradeApplyTypeEnums(int code, String label) {
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
		for (ScriptTradeApplyTypeEnums item : values()) {
			if (item.code == code) {
				return item.label;
			}
		}
		return null;
	}

	public static boolean isValid(Integer code) {
		return code != null && (code == BUY.code || code == NEGOTIATE.code);
	}
}
