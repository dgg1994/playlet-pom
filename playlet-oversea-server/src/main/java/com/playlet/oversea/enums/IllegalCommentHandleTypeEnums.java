package com.playlet.oversea.enums;

import lombok.Getter;

/**
 * 违规评论后置处置类型（illegal_comment_record.handle_type）。
 */
@Getter
public enum IllegalCommentHandleTypeEnums {

	IGNORE(1, "忽略/通过"),
	DELETE_COMMENT(2, "删除评论"),
	MUTE_USER(3, "禁言用户"),
	FREEZE_ACCOUNT(4, "冻结账户");

	private final int code;
	private final String label;

	IllegalCommentHandleTypeEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public static IllegalCommentHandleTypeEnums fromCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (IllegalCommentHandleTypeEnums item : values()) {
			if (item.code == code) {
				return item;
			}
		}
		return null;
	}
}
