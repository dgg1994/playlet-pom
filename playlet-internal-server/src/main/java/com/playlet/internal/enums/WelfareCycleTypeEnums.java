package com.playlet.internal.enums;

import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 福利任务周期类型（对应 welfare_task.cycle_type）
 */
public enum WelfareCycleTypeEnums {

	DAILY(1, "每日"),
	ONCE(2, "一次性"),
	WEEKLY(3, "每周"),
	MONTHLY(4, "每月");

	private final int code;
	private final String lable;

	WelfareCycleTypeEnums(int code, String lable) {
		this.code = code;
		this.lable = lable;
	}

	public int getCode() {
		return code;
	}

	public String getLable() {
		return lable;
	}

	public static WelfareCycleTypeEnums fromCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (WelfareCycleTypeEnums type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return null;
	}

	public static boolean isValid(Integer code) {
		return fromCode(code) != null;
	}

	public static List<DicEntity> getList() {
		List<DicEntity> list = new ArrayList<>();
		for (WelfareCycleTypeEnums type : values()) {
			DicEntity dic = new DicEntity();
			dic.setName(String.valueOf(type.code));
			dic.setLable(type.lable);
			list.add(dic);
		}
		return list;
	}
}
