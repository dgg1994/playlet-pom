package com.playlet.internal.enums;

/**
 * 系统消息受众
 */
public enum SystemMessageAudienceTypeEnums {
	ALL(1, "全员读扩散"),
	UID_LIST(2, "指定uid写收件箱");

	private final int code;
	private final String desc;

	SystemMessageAudienceTypeEnums(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}
