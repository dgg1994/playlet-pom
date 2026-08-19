package com.playlet.internal.enums;

/**
 * 剧/集审核状态（列表页签直接按本字段筛：待审=0/1，通过=2，驳回=3，申诉中=4）。
 */
public enum DramaAssetAuditStatusEnums {
	PENDING(0, "待审核"),
	UNDER_REVIEW(1, "审核中"),
	APPROVED(2, "审核通过"),
	REJECTED(3, "审核驳回"),
	APPEALING(4, "申诉中");

	private final int code;
	private final String label;

	DramaAssetAuditStatusEnums(int code, String label) {
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
		for (DramaAssetAuditStatusEnums item : values()) {
			if (item.code == code) {
				return item.label;
			}
		}
		return null;
	}

	/** 是否申诉再审中（列表「申诉」页签）。 */
	public static boolean isAppealing(Integer code) {
		return code != null && code.equals(APPEALING.code);
	}

	/** 是否已驳回。 */
	public static boolean isRejected(Integer code) {
		return code != null && code.equals(REJECTED.code);
	}

	/** 是否已审核通过。 */
	public static boolean isApproved(Integer code) {
		return code != null && code.equals(APPROVED.code);
	}
}
