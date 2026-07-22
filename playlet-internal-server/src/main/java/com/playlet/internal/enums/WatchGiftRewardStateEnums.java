package com.playlet.internal.enums;

import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 观影礼阶梯展示/领取状态
 */
public enum WatchGiftRewardStateEnums {

	DONE("done", "已领"),
	CLAIMABLE("claimable", "可领"),
	LOCKED("locked", "未达");

	private final String name;
	private final String lable;

	WatchGiftRewardStateEnums(String name, String lable) {
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
		for (WatchGiftRewardStateEnums state : values()) {
			DicEntity dic = new DicEntity();
			dic.setName(state.name);
			dic.setLable(state.lable);
			list.add(dic);
		}
		return list;
	}
}
