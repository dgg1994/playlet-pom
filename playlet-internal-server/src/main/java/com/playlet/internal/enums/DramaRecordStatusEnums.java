package com.playlet.internal.enums;

/**
 * 短剧备案状态。
 */
public enum DramaRecordStatusEnums {
	PENDING_FIRST(0, "待初审"),
	FIRST_PASSED(1, "初审通过待复审"),
	PASSED(2, "备案通过"),
	FIRST_REJECTED(3, "初审驳回"),
	SECOND_REJECTED(4, "复审驳回"),
	ON_SHELF(5, "已上架");

	private final int code;
	private final String label;

	DramaRecordStatusEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static DramaRecordStatusEnums fromCode(Integer code) {
		if (code == null) return null;
		for (DramaRecordStatusEnums item : values()) {
			if (item.code == code) return item;
		}
		return null;
	}

	public static boolean isRejected(Integer code) {
		DramaRecordStatusEnums e = fromCode(code);
		return e == FIRST_REJECTED || e == SECOND_REJECTED;
	}

	public static boolean isPassed(Integer code) {
		DramaRecordStatusEnums e = fromCode(code);
		return e == PASSED || e == ON_SHELF;
	}
}
