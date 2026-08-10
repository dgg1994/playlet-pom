package com.playlet.oversea.service;

/**
 * 提现异步打款（P0 Mock）
 */
public interface WithdrawPayoutService {

	/**
	 * 异步处理订单打款。
	 *
	 * @param orderId 订单主键
	 */
	void payoutAsync(Long orderId);
}
