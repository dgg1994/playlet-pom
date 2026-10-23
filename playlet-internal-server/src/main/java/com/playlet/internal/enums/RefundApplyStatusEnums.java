package com.playlet.internal.enums;

/**
 * 退款申请状态。
 */
public enum RefundApplyStatusEnums {

	PENDING(0, "待处理"),
	REFUNDED(1, "已退款"),
	REJECTED(2, "已拒绝");

	private final Integer index;
	private final String name;

	RefundApplyStatusEnums(Integer index, String name) {
		this.index = index;
		this.name = name;
	}

	public Integer getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public static boolean isPending(Integer status) {
		return PENDING.getIndex().equals(status);
	}
}
