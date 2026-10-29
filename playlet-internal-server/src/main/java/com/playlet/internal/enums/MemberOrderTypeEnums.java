package com.playlet.internal.enums;

/**
 * 会员订单类型。
 */
public enum MemberOrderTypeEnums {

	/** 首购（含连续包月首期） */
	NEW(1, "首购"),
	/** 自动续费代扣 */
	RENEW(2, "自动续费");

	private final Integer index;
	private final String name;

	MemberOrderTypeEnums(Integer index, String name) {
		this.index = index;
		this.name = name;
	}

	public Integer getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public static MemberOrderTypeEnums ofIndex(Integer index) {
		if (index == null) {
			return null;
		}
		for (MemberOrderTypeEnums item : values()) {
			if (item.index.equals(index)) {
				return item;
			}
		}
		return null;
	}

	public static boolean isRenew(Integer index) {
		return RENEW.equals(ofIndex(index));
	}
}
