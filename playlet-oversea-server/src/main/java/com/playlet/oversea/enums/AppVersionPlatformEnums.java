package com.playlet.oversea.enums;

import com.playlet.oversea.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * App 版本平台
 */
public enum AppVersionPlatformEnums {

	ANDROID("android", "Android"),
	IOS("ios", "iOS"),
	WEB("web", "Web"),
	WINDOWS("windows", "Windows"),
	MAC("mac", "Mac");

	private final String code;
	private final String label;

	AppVersionPlatformEnums(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static AppVersionPlatformEnums fromCode(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}
		for (AppVersionPlatformEnums e : values()) {
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
		for (AppVersionPlatformEnums e : values()) {
			DicEntity dic = new DicEntity();
			dic.setName(e.code);
			dic.setLable(e.label);
			list.add(dic);
		}
		return list;
	}
}
