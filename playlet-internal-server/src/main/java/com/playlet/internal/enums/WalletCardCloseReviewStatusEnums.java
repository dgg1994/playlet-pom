package com.playlet.internal.enums;

/**
 * 销卡申请审核状态（对齐 onetoken ReviewStatusEnums / wallet_card_close.review_status）。
 */
public enum WalletCardCloseReviewStatusEnums {

	PROCESSING(1, "注销中"),
	SUCCESS(2, "注销成功"),
	FAIL(3, "注销失败");

	private final Integer index;
	private final String name;

	WalletCardCloseReviewStatusEnums(Integer index, String name) {
		this.index = index;
		this.name = name;
	}

	public Integer getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public static String getName(Integer index) {
		if (index == null) {
			return null;
		}
		for (WalletCardCloseReviewStatusEnums e : values()) {
			if (e.index.equals(index)) {
				return e.name;
			}
		}
		return String.valueOf(index);
	}
}
