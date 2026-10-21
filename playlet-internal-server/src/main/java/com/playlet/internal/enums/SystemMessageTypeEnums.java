package com.playlet.internal.enums;

/**
 * 系统消息类型
 */
public enum SystemMessageTypeEnums {
	NOTICE("NOTICE", "官方公告"),
	ACTIVITY("ACTIVITY", "活动"),
	VERSION("VERSION", "版本说明"),
	DRAMA_ONLINE("DRAMA_ONLINE", "剧上线"),
	ACCOUNT("ACCOUNT", "账户");

	private final String code;
	private final String desc;

	SystemMessageTypeEnums(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}
