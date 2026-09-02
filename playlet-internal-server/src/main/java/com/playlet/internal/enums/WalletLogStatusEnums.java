package com.playlet.internal.enums;

/**
 * 钱包账变状态（对齐 onetoken OrderStateEnums.POSTED=2）。
 */
public enum WalletLogStatusEnums {
	PROCESSING("1"),
	POSTED("2");

	private final String code;

	WalletLogStatusEnums(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
