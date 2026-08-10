package com.playlet.oversea.enums;

/**
 * 互动消息类型
 */
public enum InteractMessageTypeEnums {
	LIKE_DRAMA("LIKE_DRAMA", "点赞短剧"),
	LIKE_COMMENT("LIKE_COMMENT", "点赞评论"),
	COMMENT_DRAMA("COMMENT_DRAMA", "评论短剧"),
	COMMENT_VIDEO("COMMENT_VIDEO", "评论视频"),
	REPLY_DRAMA("REPLY_DRAMA", "回复短剧评论"),
	REPLY_VIDEO("REPLY_VIDEO", "回复视频评论");

	private final String code;
	private final String desc;

	InteractMessageTypeEnums(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

	/** 回复类（含历史 REPLY_COMMENT） */
	public static boolean isReply(String messageType) {
		return REPLY_DRAMA.getCode().equals(messageType)
				|| REPLY_VIDEO.getCode().equals(messageType);
	}

	/** 一级评论类 */
	public static boolean isComment(String messageType) {
		return COMMENT_DRAMA.getCode().equals(messageType)
				|| COMMENT_VIDEO.getCode().equals(messageType);
	}

	/** 列表可点赞/回复操作 */
	public static boolean isActionable(String messageType) {
		return isReply(messageType) || isComment(messageType);
	}
}
