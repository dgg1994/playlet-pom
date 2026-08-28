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

	/**
	 * 将三方 status 映射为本地 KYC 状态。
	 *
	 * @param apiStatus uncommitted/waiting/success/fail/wait_confirm/wait_audit
	 */
	public static WalletKycStateEnums fromApiStatus(String apiStatus) {
		if (apiStatus == null || apiStatus.isEmpty()) {
			return WAIT_APPROVE;
		}
		switch (apiStatus) {
			case "success":
				return SUCCESS_APPROVE;
			case "fail":
				return ERROR_APPROVE;
			case "waiting":
			case "wait_confirm":
			case "wait_audit":
				return PROCESS_APPROVE;
			case "uncommitted":
			default:
				return WAIT_APPROVE;
		}
	}
}
