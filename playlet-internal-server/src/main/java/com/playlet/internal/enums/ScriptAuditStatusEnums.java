package com.playlet.internal.enums;

/**
 * 剧本审核状态：-1 草稿、0 待审、1 审核中、2 通过、3 驳回。
 */
public enum ScriptAuditStatusEnums {
	DRAFT(-1, "草稿"),
	PENDING(0, "待审核"),
	UNDER_REVIEW(1, "审核中"),
	APPROVED(2, "审核通过"),
	REJECTED(3, "审核驳回");

	private final int code;
	private final String label;

	ScriptAuditStatusEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static String getLabel(Integer code) {
		if (code == null) {
			return null;
		}
		for (ScriptAuditStatusEnums item : values()) {
			if (item.code == code) {
				return item.label;
			}
		}
		return null;
	}

	public static boolean isApproved(Integer code) {
		return code != null && code == APPROVED.code;
	}
}
