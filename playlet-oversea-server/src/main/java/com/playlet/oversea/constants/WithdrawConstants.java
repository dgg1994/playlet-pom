package com.playlet.oversea.constants;

/**
 * 提现业务常量。
 */
public final class WithdrawConstants {

	private WithdrawConstants() {
	}

	/** 历史 U 卡提现资产编码（旧订单快照） */
	public static final String ASSET_WALLET = "USD";
	/** 历史 U 卡提现网络编码（旧订单快照） */
	public static final String NETWORK_WALLET = "UCARD";

	/** 提现打款网关：入账钱包 available_balance */
	public static final String GATEWAY_BALANCE = "BALANCE";

	/** C 端提现单号前缀 */
	public static final String ORDER_NO_PREFIX_APP = "W";
	/** 作家提现单号前缀 */
	public static final String ORDER_NO_PREFIX_CREATOR = "CW";

	/** 管理端页签：未处理（待处理+打款中） */
	public static final int PROCESS_FLAG_UNPROCESSED = 0;
	/** 管理端页签：已处理（成功+失败+已退回） */
	public static final int PROCESS_FLAG_PROCESSED = 1;

	/** 管理端列表：U 卡支付方式展示文案 */
	public static final String PAY_METHOD_WALLET_LABEL = "U卡";
	/** 管理端列表：钱包余额支付方式展示文案 */
	public static final String PAY_METHOD_BALANCE_LABEL = "钱包余额";
	/** 提现记录：到账目标展示文案 */
	public static final String PAYOUT_TARGET_BALANCE_LABEL = "钱包余额";
}
