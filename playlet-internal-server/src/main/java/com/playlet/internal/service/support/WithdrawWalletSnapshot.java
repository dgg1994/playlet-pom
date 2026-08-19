package com.playlet.internal.service.support;

import lombok.Data;

/**
 * 提现钱包快照（C 端账号或作家账号）。
 */
@Data
public class WithdrawWalletSnapshot {

	private long coinBalance;
	private long frozenCoinBalance;
	private Integer onepayBindStatus;
	private String onepayAccount;
}
