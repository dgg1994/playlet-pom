package com.playlet.internal.enums;

/**
 * 弹幕展示状态（对应 drama_danmu.status）。
 */
public enum DanmuStatusEnums {

	HIDDEN(0, "隐藏/审核不通过"),
	NORMAL(1, "正常展示"),
	USER_DELETED(2, "用户自删");

	private final Integer index;
	private final String name;

	DanmuStatusEnums(Integer index, String name) {
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
