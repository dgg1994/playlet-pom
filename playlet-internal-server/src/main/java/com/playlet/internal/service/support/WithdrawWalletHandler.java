package com.playlet.internal.service.support;

import com.playlet.internal.enums.WithdrawUserTypeEnums;

/**
 * C 端 / 作家端提现钱包操作（余额、冻结、OnePay 标识、流水）。
 */
public interface WithdrawWalletHandler {

	WithdrawUserTypeEnums userType();

	WithdrawWalletSnapshot load(Integer uid);

	int freeze(Integer uid, int amt);

	int settleFrozen(Integer uid, int amt);

	int unfreeze(Integer uid, int amt);

	String findOpenId(Integer uid);

	void writeWithdrawLedger(Integer uid, int amt, String orderNo);
}
