package com.playlet.internal.enums;

import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * App 版本渠道
 */
public enum AppVersionChannelEnums {

	DEFAULT("default", "默认"),
	GOOGLEPLAY("googleplay", "Google Play"),
	APPSTORE("appstore", "App Store"),
	HUAWEI("huawei", "华为"),
	XIAOMI("xiaomi", "小米"),
	OPPO("oppo", "OPPO"),
	VIVO("vivo", "vivo");

	private final String code;
	private final String label;

	AppVersionChannelEnums(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static AppVersionChannelEnums fromCode(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}
		for (AppVersionChannelEnums e : values()) {
			if (e.code.equalsIgnoreCase(code.trim())) {
				return e;
			}
		}
		return null;
	}

	public static boolean isValid(String code) {
		return fromCode(code) != null;
	}

	public static List<DicEntity> getList() {
		List<DicEntity> list = new ArrayList<>();
		for (AppVersionChannelEnums e : values()) {
			DicEntity dic = new DicEntity();
			dic.setName(e.code);
			dic.setLable(e.label);
			list.add(dic);
		}
		return list;
	}
}
