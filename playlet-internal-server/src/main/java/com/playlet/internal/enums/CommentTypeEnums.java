package com.playlet.internal.enums;

/**
 * 评论类型（drama_video_comment.comment_type）
 */
public enum CommentTypeEnums {

	VIDEO(1, "视频评论"),
	DRAMA(2, "短剧评论");

	private final int code;
	private final String lable;

	CommentTypeEnums(int code, String lable) {
		this.code = code;
		this.lable = lable;
	}

	public int getCode() {
		return code;
	}

	public String getLable() {
		return lable;
	}

	public static CommentTypeEnums fromCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (CommentTypeEnums type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return null;
	}

	public static boolean isDrama(Integer code) {
		return code != null && code == DRAMA.code;
	}
}
