package com.playlet.oversea.enums;

import com.playlet.oversea.constants.WalletNetworkTypeConstants;
import com.playlet.oversea.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 链网络类型字典（对齐 onetoken NetworkTypeEnums，保留 findNetwokList 拼写）。
 */
public enum WalletNetworkTypeDicEnums {
	TRON(WalletNetworkTypeConstants.TRON, "TRON"),
	BSC(WalletNetworkTypeConstants.BSC, "BSC"),
	ETH("ETH", "ETH"),
	BTC("BTC", "BTC");

	private final String code;
	private final String label;

	WalletNetworkTypeDicEnums(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static List<DicEntity> getList() {
		List<DicEntity> list = new ArrayList<>();
		for (WalletNetworkTypeDicEnums item : values()) {
			DicEntity dic = new DicEntity();
			dic.setId(list.size() + 1);
			dic.setName(item.label);
			dic.setValue(item.code);
			list.add(dic);
		}
		return list;
	}
}
