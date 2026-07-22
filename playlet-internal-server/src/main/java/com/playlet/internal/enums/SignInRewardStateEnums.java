package com.playlet.internal.enums;

import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 签到奖励阶梯展示状态（对应 SignInRewardItemEntity.state）
 */
public enum SignInRewardStateEnums {

	DONE("done", "已领"),
	TODAY("today", "今日档"),
	LOCKED("locked", "未达");

	private final String name;
	private final String lable;

	SignInRewardStateEnums(String name, String lable) {
		this.name = name;
		this.lable = lable;
	}

	public String getName() {
		return name;
	}

	public String getLable() {
		return lable;
	}

	public static SignInRewardStateEnums fromName(String name) {
		if (name == null || name.isEmpty()) {
			return null;
		}
		for (SignInRewardStateEnums state : values()) {
			if (state.name.equals(name)) {
				return state;
			}
		}
		return null;
	}

	public static boolean isValid(String name) {
		return fromName(name) != null;
	}

	public static List<DicEntity> getList() {
		List<DicEntity> list = new ArrayList<>();
		for (SignInRewardStateEnums state : values()) {
			DicEntity dic = new DicEntity();
			dic.setName(state.name);
			dic.setLable(state.lable);
			list.add(dic);
		}
		return list;
	}
}
