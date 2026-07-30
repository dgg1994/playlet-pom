package com.playlet.internal.enums;

/**
 * 互动消息类型
 */
public enum InteractMessageTypeEnums {
	LIKE_DRAMA("LIKE_DRAMA"),
	LIKE_COMMENT("LIKE_COMMENT"),
	COMMENT_DRAMA("COMMENT_DRAMA"),
	COMMENT_VIDEO("COMMENT_VIDEO"),
	REPLY_COMMENT("REPLY_COMMENT");

	private final String code;

	InteractMessageTypeEnums(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
