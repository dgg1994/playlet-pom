package com.playlet.internal.enums;

/**
 * 推荐的福利任务编码（对应 welfare_task.task_code）。
 * 运营可新增库中编码；此枚举用于首版约定与校验提示，不必穷尽所有任务。
 */
public enum WelfareTaskCodeEnums {

	WATCH_EP("WATCH_EP", "每日看剧"),
	SHARE_DRAMA("SHARE_DRAMA", "分享短剧"),
	FOLLOW_USER("FOLLOW_USER", "关注用户"),
	COMPLETE_PROFILE("COMPLETE_PROFILE", "完善个人信息"),
	BIND_GOOGLE("BIND_GOOGLE", "绑定Google账号"),
	COMMENT_DRAMA("COMMENT_DRAMA", "评论短剧"),
	LIKE_DRAMA("LIKE_DRAMA", "点赞短剧"),
	INVITE_USER("INVITE_USER", "邀请用户"),
	RECHARGE_FIRST("RECHARGE_FIRST", "首次充值"),
	SIGN_IN("SIGN_IN", "每日签到");

	private final String code;
	private final String lable;

	WelfareTaskCodeEnums(String code, String lable) {
		this.code = code;
		this.lable = lable;
	}

	public String getCode() {
		return code;
	}

	public String getLable() {
		return lable;
	}

	public static WelfareTaskCodeEnums fromCode(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}
		for (WelfareTaskCodeEnums e : values()) {
			if (e.code.equalsIgnoreCase(code.trim())) {
				return e;
			}
		}
		return null;
	}
}
