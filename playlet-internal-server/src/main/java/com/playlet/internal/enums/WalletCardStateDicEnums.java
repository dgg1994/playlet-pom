package com.playlet.internal.enums;

import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理端卡状态下拉（对齐 onetoken CardStateEnums 常用子集）。
 */
public enum WalletCardStateDicEnums {
	INITIAL(WalletCardStatusEnums.INITIAL),
	WAIT_ACTIVE(WalletCardStatusEnums.WAIT_ACTIVE),
	ACTIVATING(WalletCardStatusEnums.ACTIVATING),
	ACTIVE(WalletCardStatusEnums.ACTIVE),
	FREEZE(WalletCardStatusEnums.FREEZE),
	CLOSED(WalletCardStatusEnums.CLOSED);

	private final WalletCardStatusEnums status;

	WalletCardStateDicEnums(WalletCardStatusEnums status) {
		this.status = status;
	}

	public int getCode() {
		return status.getCode();
	}

	public String getLabel() {
		return status.getLabel();
	}

	public static List<DicEntity> getList() {
		List<DicEntity> list = new ArrayList<>();
		for (WalletCardStateDicEnums item : values()) {
			DicEntity dic = new DicEntity();
			dic.setId(item.getCode());
			dic.setName(item.getLabel());
			list.add(dic);
		}
		return list;
	}
}
