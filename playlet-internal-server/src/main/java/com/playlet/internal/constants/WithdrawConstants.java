package com.playlet.internal.constants;

/**
 * 提现业务常量。
 */
public final class WithdrawConstants {

	private WithdrawConstants() {
	}

	/** OnePay 提现资产编码（写入订单快照） */
	public static final String ASSET_ONEPAY = "ONEPAY";
	/** OnePay 提现网络编码 */
	public static final String NETWORK_ONEPAY = "ONEPAY";

	/** OnePay 回调：成功 */
	public static final int CALLBACK_SUCCESS = 1;
	/** OnePay 回调：失败 */
	public static final int CALLBACK_FAIL = 0;
}
