package com.playlet.oversea.enums;

/**
 * 实体卡物流状态（对齐 worldpay LogisticsStateEnums）。
 */
public enum WalletLogisticsStateEnums {

	WAIT_SUCCESS(1, "待发货"),
	ALREADY_SHIPPING(2, "已发货"),
	INTRANSIT(3, "运输中"),
	OUTFORDELIVERY(4, "配送中"),
	DELIVERED(5, "完成");

	private final int code;
	private final String label;

	WalletLogisticsStateEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static WalletLogisticsStateEnums fromCode(Integer code) {
		if (code == null) {
			return WAIT_SUCCESS;
		}
		for (WalletLogisticsStateEnums item : values()) {
			if (item.code == code) {
				return item;
			}
		}
		return WAIT_SUCCESS;
	}
}
