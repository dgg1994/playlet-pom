package com.playlet.oversea.enums;

/**
 * 提现订单状态
 */
public enum WithdrawOrderStatusEnums {
	PENDING(0, "待处理"),
	PAYING(1, "打款中"),
	SUCCESS(2, "成功"),
	FAILED(3, "失败"),
	REFUNDED(4, "已退回");

	private final int code;
	private final String lable;

	WithdrawOrderStatusEnums(int code, String lable) {
		this.code = code;
		this.lable = lable;
	}

	public int getCode() {
		return code;
	}

	public String getLable() {
		return lable;
	}

	public static WithdrawOrderStatusEnums fromCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (WithdrawOrderStatusEnums e : values()) {
			if (e.code == code) {
				return e;
			}
		}
		return null;
	}

	public static String getLableByCode(Integer code) {
		WithdrawOrderStatusEnums e = fromCode(code);
		return e == null ? null : e.getLable();
	}
}
