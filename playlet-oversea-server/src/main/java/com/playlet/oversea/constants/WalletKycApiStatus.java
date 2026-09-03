package com.playlet.oversea.constants;

/**
 * worldPay KYC 三方状态（文档 GET /api/user/kyc/status）。
 */
public final class WalletKycApiStatus {

	private WalletKycApiStatus() {
	}

	/** 未提交 */
	public static final String UNCOMMITTED = "uncommitted";
	/** 待审核 */
	public static final String WAITING = "waiting";
	/** 审核成功 */
	public static final String SUCCESS = "success";
	/** 审核失败 */
	public static final String FAIL = "fail";
	/** 待用户确认 */
	public static final String WAIT_CONFIRM = "wait_confirm";
	/** 审核中 */
	public static final String WAIT_AUDIT = "wait_audit";
}
