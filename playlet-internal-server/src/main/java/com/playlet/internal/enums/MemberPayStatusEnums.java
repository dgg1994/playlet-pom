package com.playlet.internal.enums;

/**
 * 会员/解锁订单支付状态。
 */
public enum MemberPayStatusEnums {

	WAIT(0, "待支付"),
	PAID(1, "已支付"),
	CLOSED(2, "已关闭"),
	REFUNDED(3, "已退款");

	private final Integer index;
	private final String name;

	MemberPayStatusEnums(Integer index, String name) {
		this.index = index;
		this.name = name;
	}

	public Integer getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public static boolean isPaid(Integer status) {
		return PAID.getIndex().equals(status);
	}
}
