package com.playlet.internal.enums;

import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户福利任务进度状态（对应 user_welfare_progress.progress_status；
 * NOT_ACCEPTED 仅列表展示，不落库）
 */
public enum WelfareProgressStatusEnums {

	NOT_ACCEPTED(-1, "未领取任务"),
	DOING(0, "进行中"),
	CLAIMABLE(1, "可领取奖励"),
	CLAIMED(2, "奖励已领取"),
	EXPIRED(3, "已过期"),
	ABANDONED(4, "已放弃");

	private final int code;
	private final String lable;

	WelfareProgressStatusEnums(int code, String lable) {
		this.code = code;
		this.lable = lable;
	}

	public int getCode() {
		return code;
	}

	public String getLable() {
		return lable;
	}

	public static WelfareProgressStatusEnums fromCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (WelfareProgressStatusEnums status : values()) {
			if (status.code == code) {
				return status;
			}
		}
		return null;
	}

	public static boolean isValid(Integer code) {
		return fromCode(code) != null;
	}

	/** 是否已结束，不再累计进度 */
	public static boolean isTerminal(Integer code) {
		WelfareProgressStatusEnums status = fromCode(code);
		return status == CLAIMABLE || status == CLAIMED || status == EXPIRED || status == ABANDONED;
	}

	public static List<DicEntity> getList() {
		List<DicEntity> list = new ArrayList<>();
		for (WelfareProgressStatusEnums status : values()) {
			DicEntity dic = new DicEntity();
			dic.setName(String.valueOf(status.code));
			dic.setLable(status.lable);
			list.add(dic);
		}
		return list;
	}
}
