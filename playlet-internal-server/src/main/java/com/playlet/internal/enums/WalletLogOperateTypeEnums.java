package com.playlet.internal.enums;

/**
 * 钱包账变操作类型（与 onetoken WalletTitleTypeEnums 索引对齐）。
 */
public enum WalletLogOperateTypeEnums {
	WALLET_TOP_UP(1, "wallet.log.wallet_top_up"),
	OPEN_CARD(3, "wallet.log.open_card"),
	CARD_TOP_UP(2, "wallet.log.card_top_up"),
	INTERNAL_TRANSFER_IN(5, "wallet.log.internal_transfer_in"),
	INTERNAL_TRANSFER_OUT(6, "wallet.log.internal_transfer_out"),
	CARD_CLOSE(7, "wallet.log.card_close"),
	OPEN_CARD_FREEZE(8, "wallet.log.open_card_freeze"),
	OPEN_CARD_THAW(9, "wallet.log.open_card_thaw"),
	SYS_TOP_UP(10, "wallet.log.sys_top_up");

	private final int code;
	private final String i18nKey;

	WalletLogOperateTypeEnums(int code, String i18nKey) {
		this.code = code;
		this.i18nKey = i18nKey;
	}

	public int getCode() {
		return code;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public static WalletLogOperateTypeEnums fromCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (WalletLogOperateTypeEnums item : values()) {
			if (item.code == code) {
				return item;
			}
		}
		return null;
	}
}
