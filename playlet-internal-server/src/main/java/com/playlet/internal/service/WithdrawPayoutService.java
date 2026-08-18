package com.playlet.internal.service;

/**
 * OnePay 提现打款：异步受理 + 回调确认后解冻/扣减。
 */
public interface WithdrawPayoutService {

	/**
	 * 异步向 OnePay 发起打款。
	 *
	 * @param orderId 提现订单主键
	 */
	void payoutAsync(Long orderId);

	/**
	 * OnePay 确认结果：成功则解冻并扣减金币，失败则仅解冻。
	 *
	 * @param orderNo 业务单号
	 * @param success true 到账成功
	 * @param thirdOrderNo 三方流水号
	 * @param failReason 失败原因
	 */
	void handleCallback(String orderNo, boolean success, String thirdOrderNo, String failReason);
}
