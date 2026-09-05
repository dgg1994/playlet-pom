package com.playlet.oversea.constants;

/**
 * 钱包系统消息 / 极光跳转常量。
 */
public final class WalletNotifyConstants {

	private WalletNotifyConstants() {
	}

	/** 系统消息类型 */
	public static final String MESSAGE_TYPE = "WALLET";

	/** 跳转：钱包首页 */
	public static final String JUMP_HOME = "wallet_home";
	/** 跳转：钱包账变 */
	public static final String JUMP_LOG = "wallet_log";
	/** 跳转：卡详情（jumpParam=本地 wallet_bankcard.id 或三方 userBankcardId） */
	public static final String JUMP_CARD = "wallet_card";
	/** 跳转：KYC */
	public static final String JUMP_KYC = "wallet_kyc";
	/** 跳转：转账记录 */
	public static final String JUMP_TRANSFER = "wallet_transfer";
	/** 跳转：提现/入账 */
	public static final String JUMP_WITHDRAW = "wallet_withdraw";
	/** 跳转：开卡申请/物流（jumpParam=applyId） */
	public static final String JUMP_APPLY = "wallet_apply";
}
