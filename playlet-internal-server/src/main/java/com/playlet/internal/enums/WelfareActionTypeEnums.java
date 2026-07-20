package com.playlet.internal.enums;

import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 福利任务行为类型（完成方式 / 推进路由），对应 extra_config.actionType。
 * 与 task_code（任务身份）不同：同一 actionType 可对应多条任务。
 */
public enum WelfareActionTypeEnums {

	WATCH("WATCH", "观看", true),
	SHARE("SHARE", "分享", true),
	FOLLOW("FOLLOW", "关注", true),
	COMMENT("COMMENT", "评论", true),
	LIKE("LIKE", "点赞", true),
	INVITE("INVITE", "邀请", true),
	RECHARGE("RECHARGE", "充值", true),
	SIGN_IN("SIGN_IN", "签到", true),
	COMPLETE_PROFILE("COMPLETE_PROFILE", "完善个人信息", true),
	BIND_GOOGLE("BIND_GOOGLE", "绑定Google", true),
	MANUAL("MANUAL", "仅配置/手动，无自动推进", false);

	private final String name;
	private final String lable;
	private final boolean autoProgress;

	WelfareActionTypeEnums(String name, String lable, boolean autoProgress) {
		this.name = name;
		this.lable = lable;
		this.autoProgress = autoProgress;
	}

	public String getName() {
		return name;
	}

	public String getLable() {
		return lable;
	}

	/** 是否支持 onAction 自动推进 */
	public boolean isAutoProgress() {
		return autoProgress;
	}

	public static WelfareActionTypeEnums fromName(String name) {
		if (name == null || name.isEmpty()) {
			return null;
		}
		for (WelfareActionTypeEnums typeEnum : values()) {
			if (typeEnum.name.equalsIgnoreCase(name.trim())) {
				return typeEnum;
			}
		}
		return null;
	}

	public static String getLableByName(String name) {
		WelfareActionTypeEnums type = fromName(name);
		return type == null ? null : type.getLable();
	}

	public static List<DicEntity> getList() {
		List<DicEntity> list = new ArrayList<>();
		for (WelfareActionTypeEnums typeEnum : values()) {
			DicEntity dicEntity = new DicEntity();
			dicEntity.setName(typeEnum.getName());
			dicEntity.setLable(typeEnum.getLable());
			list.add(dicEntity);
		}
		return list;
	}

	/** 管理端/字典：仅返回已支持自动推进或常用类型 */
	public static List<DicEntity> getSupportedList() {
		List<DicEntity> list = new ArrayList<>();
		for (WelfareActionTypeEnums typeEnum : values()) {
			DicEntity dicEntity = new DicEntity();
			dicEntity.setName(typeEnum.getName());
			dicEntity.setLable(typeEnum.getLable());
			list.add(dicEntity);
		}
		return list;
	}
}
