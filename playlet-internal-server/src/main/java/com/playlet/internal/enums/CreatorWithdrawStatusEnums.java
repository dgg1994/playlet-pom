package com.playlet.internal.enums;

/**
 * 创作者提现状态。
 */
public enum CreatorWithdrawStatusEnums {

	/** 处理中 */
	PROCESSING(0, "处理中"),
	/** 已到账 */
	ARRIVED(1, "已到账"),
	/** 已失败 */
	FAILED(2, "已失败");

	private final int code;
	private final String label;

	CreatorWithdrawStatusEnums(int code, String label) {
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
			return "";
		}
		for (CreatorWithdrawStatusEnums item : values()) {
			if (item.code == code) {
				return item.label;
			}
		}
		return "";
	}

	/** 前端筛选项文案 → code，null 表示全部 */
	public static Integer codeOfLabel(String label) {
		if (label == null || label.trim().isEmpty() || "全部".equals(label.trim())) {
			return null;
		}
		for (CreatorWithdrawStatusEnums item : values()) {
			if (item.label.equals(label.trim())) {
				return item.code;
			}
		}
		return null;
	}
}
