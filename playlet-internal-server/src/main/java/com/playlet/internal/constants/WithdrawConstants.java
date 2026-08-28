package com.playlet.internal.constants;

/**
 * 提现业务常量。
 */
public final class WithdrawConstants {

	private WithdrawConstants() {
	}

	/** U 卡提现资产编码（写入订单快照） */
	public static final String ASSET_WALLET = "USD";
	/** U 卡提现网络编码 */
	public static final String NETWORK_WALLET = "UCARD";

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
}
