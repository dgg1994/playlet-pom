package com.playlet.internal.enums;

import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 充值/支付方式字典（对齐 onetoken PayTypeEnums）。
 */
public enum WalletPayTypeEnums {
	WALLET(WalletConstants.TOPUP_TYPE_WALLET, "钱包"),
	BANKCARD(WalletConstants.TOPUP_TYPE_BANKCARD, "银行卡");

	private final int code;
	private final String label;

	WalletPayTypeEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static List<DicEntity> getList() {
		List<DicEntity> list = new ArrayList<>();
		for (WalletPayTypeEnums item : values()) {
			DicEntity dic = new DicEntity();
			dic.setId(item.code);
			dic.setName(item.label);
			list.add(dic);
		}
		return list;
	}
}
