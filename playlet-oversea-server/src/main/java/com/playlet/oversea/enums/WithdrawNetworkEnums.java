package com.playlet.oversea.enums;

/**
 * 提现资产网络
 */
public enum WithdrawNetworkEnums {
	TRC20("TRC20"),
	ERC20("ERC20");

	private final String code;

	WithdrawNetworkEnums(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static WithdrawNetworkEnums fromCode(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}
		for (WithdrawNetworkEnums e : values()) {
			if (e.code.equalsIgnoreCase(code.trim())) {
				return e;
			}
		}
		return null;
	}
}
