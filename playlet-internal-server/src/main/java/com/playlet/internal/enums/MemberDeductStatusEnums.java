package com.playlet.internal.enums;

/**
 * 代扣结果状态（member_order.deduct_status）。
 */
public enum MemberDeductStatusEnums {

	/** 未扣款（含首购走普通下单的订单） */
	INIT(0, "未扣款"),
	/** 代扣成功 */
	SUCCESS(1, "成功"),
	/** 代扣失败 */
	FAIL(2, "失败");

	private final Integer index;
	private final String name;

	MemberDeductStatusEnums(Integer index, String name) {
		this.index = index;
		this.name = name;
	}

	public Integer getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public static boolean isSuccess(Integer index) {
		return SUCCESS.equals(index);
	}
}
