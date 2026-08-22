package com.playlet.oversea.service.support;

import com.playlet.oversea.enums.WithdrawUserTypeEnums;

/**
 * C 端 / 作家端提现钱包操作（余额、冻结、OnePay 标识、流水）。
 */
public interface WithdrawWalletHandler {

	/**
	 * 提现用户类型。
	 */
	WithdrawUserTypeEnums userType();

	/**
	 * 加载提现钱包快照。
	 */
	WithdrawWalletSnapshot load(Integer uid);

	/**
	 * 冻结提现钱包。
	 */
	int freeze(Integer uid, int amt);

	/**
	 * 结算冻结提现钱包。
	 */
	int settleFrozen(Integer uid, int amt);

	/**
	 * 解冻提现钱包。
	 */
	int unfreeze(Integer uid, int amt);

	/**
	 * 查询提现钱包的 OnePay 账号。
	 */
	String findOpenId(Integer uid);

	/**
	 * 写入提现冻结流水。
	 */
	void writeWithdrawFreezeLedger(Integer uid, int amt, String orderNo);

	/**
	 * 写入提现流水。
	 */
	void writeWithdrawLedger(Integer uid, int amt, String orderNo);

	/**
	 * 写入提现退款流水。
	 */
	void writeWithdrawRefundLedger(Integer uid, int amt, String orderNo);
}
