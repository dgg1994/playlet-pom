package com.playlet.oversea.enums;

/**
 * 提现主体：C 端与作家 ID 空间独立，订单必须带类型。
 */
public enum WithdrawUserTypeEnums {
	APP(1, "C端用户"),
	CREATOR(2, "作家");

	private final int code;
	private final String label;

	WithdrawUserTypeEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static WithdrawUserTypeEnums fromCode(Integer code) {
		if (code == null) {
			return APP;
		}
		for (WithdrawUserTypeEnums item : values()) {
			if (item.code == code) {
				return item;
			}
		}
		return APP;
	}
}
