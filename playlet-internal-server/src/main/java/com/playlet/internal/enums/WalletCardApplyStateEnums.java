package com.playlet.internal.enums;

/**
 * 开卡申请状态（对齐 worldpay OpenCardStateEnums）。
 */
public enum WalletCardApplyStateEnums {

	WAIT_ACTIVATION(1, "待激活"),
	PROCESS_ACTIVATION(2, "激活中"),
	SUCCESS_ACTIVATION(3, "激活成功"),
	ERROR_ACTIVATION(4, "激活失败"),
	WAIT_USER_ACTIVATION(5, "待用户充值激活");

	private final int code;
	private final String label;

	WalletCardApplyStateEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static WalletCardApplyStateEnums fromCode(Integer code) {
		if (code == null) {
			return WAIT_ACTIVATION;
		}
		for (WalletCardApplyStateEnums item : values()) {
			if (item.code == code) {
				return item;
			}
		}
		return WAIT_ACTIVATION;
	}
}
