package com.playlet.internal.enums;

/**
 * 剧本售卖状态：0 待售、1 议价中、2 已售、3 签约中。
 */
public enum ScriptSaleStatusEnums {
	ON_SALE(0, "待售"),
	NEGOTIATING(1, "议价中"),
	SOLD(2, "已售"),
	SIGNING(3, "签约中");

	private final int code;
	private final String label;

	ScriptSaleStatusEnums(int code, String label) {
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
		for (ScriptSaleStatusEnums item : values()) {
			if (item.code == code) {
				return item.label;
			}
		}
		return null;
	}

	/** 是否允许发起购买/议价申请 */
	public static boolean canApply(Integer code) {
		return code != null && (code == ON_SALE.code || code == NEGOTIATING.code);
	}
}
