package com.playlet.internal.enums;

import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 签到月历单日状态
 */
public enum SignInCalendarDayStateEnums {

	SIGNED("signed", "已签"),
	MAKEUP("makeup", "可补签"),
	TODAY("today", "今日未签"),
	EMPTY("empty", "不可操作");

	private final String name;
	private final String lable;

	SignInCalendarDayStateEnums(String name, String lable) {
		this.name = name;
		this.lable = lable;
	}

	public String getName() {
		return name;
	}

	public String getLable() {
		return lable;
	}

	public static List<DicEntity> getList() {
		List<DicEntity> list = new ArrayList<>();
		for (SignInCalendarDayStateEnums state : values()) {
			DicEntity dic = new DicEntity();
			dic.setName(state.name);
			dic.setLable(state.lable);
			list.add(dic);
		}
		return list;
	}
}
