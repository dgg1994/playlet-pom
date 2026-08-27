package com.playlet.internal.enums;

/**
 * 钱包 KYC 本地状态（对齐 onetoken KycStateEnums）。
 */
public enum WalletKycStateEnums {
	WAIT_APPROVE(1, "待认证"),
	PROCESS_APPROVE(2, "认证中"),
	SUCCESS_APPROVE(3, "认证成功"),
	ERROR_APPROVE(4, "认证失败");

	private final int code;
	private final String label;

	WalletKycStateEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static WalletKycStateEnums fromCode(Integer code) {
		if (code == null) {
			return WAIT_APPROVE;
		}
		for (WalletKycStateEnums item : values()) {
			if (item.code == code) {
				return item;
			}
		}
		return WAIT_APPROVE;
	}
}
