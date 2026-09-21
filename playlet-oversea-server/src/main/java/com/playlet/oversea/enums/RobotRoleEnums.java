package com.playlet.oversea.enums;

/**
 * 机器人通知角色（对齐 worldpay；当前 Telegram 仅单群，角色仅作语义标记）。
 */
public enum RobotRoleEnums {
	FINANCE(1, "finance"),
	CUSTOMER_SERVICE(2, "customerService");

	private final Integer index;
	private final String name;

	RobotRoleEnums(Integer index, String name) {
		this.index = index;
		this.name = name;
	}

	public Integer getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}
}
