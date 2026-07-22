package com.playlet.internal.enums;

import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 金币流水业务类型（对应 user_coin_ledger.biz_type）。
 */
public enum CoinBizTypeEnums {

	TASK_REWARD("TASK_REWARD", "任务奖励"),
	AD_BOOST("AD_BOOST", "广告加赠"),
	SIGN_IN("SIGN_IN", "签到"),
	WATCH_GIFT("WATCH_GIFT", "观影礼"),
	RECHARGE("RECHARGE", "充值"),
	CONSUME("CONSUME", "消费"),
	SYSTEM("SYSTEM", "系统调整");

	private String name;
	private String lable;

	CoinBizTypeEnums(String name, String lable) {
		this.name = name;
		this.lable = lable;
	}

	public String getName() {
		return name;
	}

	public String getLable() {
		return lable;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLable(String lable) {
		this.lable = lable;
	}

	public static String getLableByName(String name) {
		if (name == null) {
			return null;
		}
		for (CoinBizTypeEnums typeEnum : values()) {
			if (typeEnum.getName().equals(name)) {
				return typeEnum.getLable();
			}
		}
		return null;
	}

	public static CoinBizTypeEnums fromName(String name) {
		if (name == null || name.isEmpty()) {
			return null;
		}
		for (CoinBizTypeEnums typeEnum : values()) {
			if (typeEnum.getName().equals(name)) {
				return typeEnum;
			}
		}
		return null;
	}

	public static List<DicEntity> getList() {
		CoinBizTypeEnums[] typeEnums = values();
		List<DicEntity> list = new ArrayList<>();
		for (CoinBizTypeEnums typeEnum : typeEnums) {
			DicEntity dicEntity = new DicEntity();
			dicEntity.setName(typeEnum.getName());
			dicEntity.setLable(typeEnum.getLable());
			list.add(dicEntity);
		}
		return list;
	}
}
