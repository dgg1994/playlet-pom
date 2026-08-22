package com.playlet.oversea.enums;

/**
 *  上架状态枚举
 */
public enum DramaAssetShelfStatusEnums {
	OFF(0, "未上架"),
	ON(1, "已上架");

	private final int code;
	private final String label;

	DramaAssetShelfStatusEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static DramaAssetShelfStatusEnums fromCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (DramaAssetShelfStatusEnums item : values()) {
			if (item.code == code) {
				return item;
			}
		}
		return null;
	}
}
