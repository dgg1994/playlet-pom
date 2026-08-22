package com.playlet.oversea.enums;

import lombok.Getter;

/**
 * 违规评论处理状态（illegal_comment_record.status）。
 */
@Getter
public enum IllegalCommentStatusEnums {

	PENDING(0, "待处理"),
	APPROVED(1, "已通过"),
	DELETED(2, "已删除"),
	MUTED(3, "已禁言用户"),
	FROZEN(4, "已冻结账户");

	private final int code;
	private final String label;

	IllegalCommentStatusEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public static IllegalCommentStatusEnums fromHandleType(IllegalCommentHandleTypeEnums handleType) {
		if (handleType == null) {
			return null;
		}
		switch (handleType) {
			case IGNORE:
				return APPROVED;
			case DELETE_COMMENT:
				return DELETED;
			case MUTE_USER:
				return MUTED;
			case FREEZE_ACCOUNT:
				return FROZEN;
			default:
				return null;
		}
	}
}
