package com.playlet.internal.enums;

/**
 * 弹幕展示模式（对应 drama_danmu.mode）。
 */
public enum DanmuModeEnums {

	SCROLL(1, "滚动"),
	TOP(2, "顶部"),
	BOTTOM(3, "底部");

	private final Integer index;
	private final String name;

	DanmuModeEnums(Integer index, String name) {
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
