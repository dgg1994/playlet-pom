package com.playlet.oversea.enums;

/**
 * 持卡人性别（对齐 worldpay SexEnums，FEMALE 修正为 2）。
 */
public enum WalletSexEnums {

	MALE(1, "男"),
	FEMALE(2, "女");

	private final int code;
	private final String label;

	WalletSexEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static String labelOf(Integer code) {
		if (code == null) {
			return null;
		}
		for (WalletSexEnums item : values()) {
			if (item.code == code) {
				return item.label;
			}
		}
		return null;
	}
}
