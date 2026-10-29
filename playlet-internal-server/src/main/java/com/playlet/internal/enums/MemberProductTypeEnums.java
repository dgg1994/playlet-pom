package com.playlet.internal.enums;

/**
 * 会员套餐类型。
 */
public enum MemberProductTypeEnums {

	/** 一次性购买（买断 N 天） */
	ONCE(1, "一次性"),
	/** 连续包月（签约后每期自动扣款） */
	AUTO_RENEW(2, "连续包月");

	private final Integer index;
	private final String name;

	MemberProductTypeEnums(Integer index, String name) {
		this.index = index;
		this.name = name;
	}

	public Integer getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public static MemberProductTypeEnums ofIndex(Integer index) {
		if (index == null) {
			return null;
		}
		for (MemberProductTypeEnums item : values()) {
			if (item.index.equals(index)) {
				return item;
			}
		}
		return null;
	}

	/** 是否连续包月商品；空值按一次性处理 */
	public static boolean isAutoRenew(Integer index) {
		return AUTO_RENEW.equals(ofIndex(index));
	}
}
