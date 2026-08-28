package com.playlet.internal.service.support;

import lombok.Data;

/**
 * 提现钱包快照：余额 + U 卡打款目标。
 */
@Data
public class WithdrawWalletSnapshot {

	private long coinBalance;
	private long frozenCoinBalance;

	/** 0 未就绪 1 可提现到 U 卡（KYC 通过 + 可用卡） */
	private Integer walletWithdrawReady;

	private Long walletUid;
	private Long targetBankcardId;
	private Long targetUserBankcardId;
	private String payoutTargetMasked;
}
