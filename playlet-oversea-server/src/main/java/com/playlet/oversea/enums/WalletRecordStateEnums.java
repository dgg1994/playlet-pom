package com.playlet.oversea.enums;

import com.playlet.oversea.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 提现/链上审核状态（对齐 onetoken ApplicationRecordStateEnums 管理端子集）。
 */
public enum WalletRecordStateEnums {
	UNDER_REVIEW(0, "待审核"),
	AUDIT_PASS(1, "完成"),
	AUDIT_FAIL(2, "不通过"),
	AUDIT_PASS_PROCESSING(3, "处理中");

	private final int code;
	private final String label;

	WalletRecordStateEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static String labelOf(Integer code) {
		if (code == null) {
			return null;
		}
		for (WalletRecordStateEnums item : values()) {
			if (item.code == code) {
				return item.label;
			}
		}
		return String.valueOf(code);
	}

	public static List<DicEntity> getList() {
		List<DicEntity> list = new ArrayList<>();
		for (WalletRecordStateEnums item : values()) {
			DicEntity dic = new DicEntity();
			dic.setId(item.code);
			dic.setName(item.label);
			list.add(dic);
		}
		return list;
	}
}
