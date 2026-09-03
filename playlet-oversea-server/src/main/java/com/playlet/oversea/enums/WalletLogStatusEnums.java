package com.playlet.oversea.enums;

/**
 * 钱包流水状态（wallet_log.status 与 wallet_card_transaction.order_state 统一编码）。
 * <p>1=处理中，2=成功，3=失败（仅卡交易 webhook 消费/退款失败场景）。</p>
 */
public enum WalletLogStatusEnums {
	PROCESSING(1, "处理中"),
	POSTED(2, "成功"),
	FAILED(3, "失败");

	private final int intCode;
	private final String label;

	WalletLogStatusEnums(int intCode, String label) {
		this.intCode = intCode;
		this.label = label;
	}

	/** wallet_log.status 存库值 */
	public String getCode() {
		return String.valueOf(intCode);
	}

	/** wallet_card_transaction.order_state 存库值 */
	public int getIntCode() {
		return intCode;
	}

	public String getLabel() {
		return label;
	}

	public static WalletLogStatusEnums fromCode(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}
		try {
			return fromIntCode(Integer.parseInt(code.trim()));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static WalletLogStatusEnums fromIntCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (WalletLogStatusEnums item : values()) {
			if (item.intCode == code) {
				return item;
			}
		}
		return null;
	}
}
