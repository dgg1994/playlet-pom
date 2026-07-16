package com.playlet.internal.enums;

/**
 * 视频方向：1竖屏 2横屏
 */
public enum DramaOrientationEnums {
	VERTICAL(1, "竖屏"),
	HORIZONTAL(2, "横屏");

	private final Integer index;
	private final String name;

	DramaOrientationEnums(Integer index, String name) {
		this.index = index;
		this.name = name;
	}

	public Integer getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public static Integer normalize(Integer orientation) {
		if (orientation == null) {
			return VERTICAL.getIndex();
		}
		if (HORIZONTAL.getIndex().equals(orientation)) {
			return HORIZONTAL.getIndex();
		}
		return VERTICAL.getIndex();
	}
}
