package com.playlet.oversea.enums;

/**
 * 系统消息发布状态
 */
public enum SystemMessagePublishStatusEnums {
	DRAFT(0, "草稿"),
	PUBLISHED(1, "已发布"),
	CANCELLED(2, "取消");

	private final int code;
	private final String desc;

	SystemMessagePublishStatusEnums(int code, String desc) {
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
