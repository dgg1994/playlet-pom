package com.playlet.internal.enums;

/**
 * 购买/议价申请状态：0 待处理、1 已同意、2 已拒绝、3 已完成、4 已取消、5 已失效。
 */
public enum ScriptTradeApplyStatusEnums {
	PENDING(0, "待处理"),
	AGREED(1, "已同意"),
	REJECTED(2, "已拒绝"),
	COMPLETED(3, "已完成"),
	CANCELED(4, "已取消"),
	EXPIRED(5, "已失效");

	private final int code;
	private final String label;

	ScriptTradeApplyStatusEnums(int code, String label) {
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
		for (ScriptTradeApplyStatusEnums item : values()) {
			if (item.code == code) {
				return item.label;
			}
		}
		return null;
	}
}
