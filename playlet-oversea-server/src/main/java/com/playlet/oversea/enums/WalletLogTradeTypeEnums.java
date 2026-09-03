package com.playlet.oversea.enums;

/**
 * 钱包账变方向：1 进账 / 2 出账。
 */
public enum WalletLogTradeTypeEnums {
	INCOME(1),
	EXPENDITURE(2);

	private final int code;

	WalletLogTradeTypeEnums(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
