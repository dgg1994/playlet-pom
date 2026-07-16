package com.playlet.internal.enums;

/**
 * 剧资源类型
 */
public enum DramaAssetTypeEnums {
	COVER_V("COVER_V", "竖封面"),
	COVER_H("COVER_H", "横封面"),
	POSTER("POSTER", "剧场海报"),
	SHARE("SHARE", "分享图");

	private final String code;
	private final String name;

	DramaAssetTypeEnums(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static String coverByOrientation(Integer orientation) {
		if (DramaOrientationEnums.HORIZONTAL.getIndex().equals(orientation)) {
			return COVER_H.getCode();
		}
		return COVER_V.getCode();
	}
}
