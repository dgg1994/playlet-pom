package com.playlet.internal.enums;

import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 签到类型（对应 user_sign_in_log.sign_type）
 */
public enum SignInTypeEnums {

	NORMAL(1, "正常签到"),
	MAKEUP(2, "补签");

	private final int code;
	private final String lable;

	SignInTypeEnums(int code, String lable) {
		this.code = code;
		this.lable = lable;
	}

	public int getCode() {
		return code;
	}

	public String getLable() {
		return lable;
	}

	public static SignInTypeEnums fromCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (SignInTypeEnums type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return null;
	}

	public static List<DicEntity> getList() {
		List<DicEntity> list = new ArrayList<>();
		for (SignInTypeEnums type : values()) {
			DicEntity dic = new DicEntity();
			dic.setName(String.valueOf(type.code));
			dic.setLable(type.lable);
			list.add(dic);
		}
		return list;
	}
}
