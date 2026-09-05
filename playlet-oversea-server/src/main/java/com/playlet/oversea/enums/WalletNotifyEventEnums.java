package com.playlet.oversea.enums;

import com.playlet.oversea.constants.WalletNotifyConstants;

/**
 * 钱包系统消息 / 极光推送事件。
 */
public enum WalletNotifyEventEnums {

	COIN_TO_WALLET_SUCCESS(WalletNotifyConstants.JUMP_WITHDRAW),
	USDT_TOPIN_SUCCESS(WalletNotifyConstants.JUMP_LOG),
	CARD_RECHARGE_SUCCESS(WalletNotifyConstants.JUMP_CARD),
	CARD_RECHARGE_FAIL(WalletNotifyConstants.JUMP_CARD),
	TRANSFER_OUT_SUCCESS(WalletNotifyConstants.JUMP_TRANSFER),
	TRANSFER_IN_SUCCESS(WalletNotifyConstants.JUMP_TRANSFER),
	KYC_PASS(WalletNotifyConstants.JUMP_KYC),
	KYC_REJECT(WalletNotifyConstants.JUMP_KYC),
	CARD_OPEN_SUCCESS(WalletNotifyConstants.JUMP_CARD),
	CARD_OPEN_FAIL(WalletNotifyConstants.JUMP_APPLY),
	CARD_FREEZE(WalletNotifyConstants.JUMP_CARD),
	CARD_UNFREEZE(WalletNotifyConstants.JUMP_CARD),
	CARD_CLOSE(WalletNotifyConstants.JUMP_CARD),
	CARD_TXN(WalletNotifyConstants.JUMP_CARD),
	CARD_3DS(WalletNotifyConstants.JUMP_CARD),
	CARD_SHIPPING(WalletNotifyConstants.JUMP_APPLY),
	PAY_PASSWORD_BOUND(WalletNotifyConstants.JUMP_HOME);

	private final String defaultJumpType;

	WalletNotifyEventEnums(String defaultJumpType) {
		this.defaultJumpType = defaultJumpType;
	}

	public String getDefaultJumpType() {
		return defaultJumpType;
	}
}
