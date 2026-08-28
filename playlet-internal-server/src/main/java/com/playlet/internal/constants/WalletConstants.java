package com.playlet.internal.constants;

/**
 * 钱包业务常量。
 */
public final class WalletConstants {

	private WalletConstants() {
	}

	/** 支付密码：6 位数字 */
	public static final String PAY_PASSWORD_REGEX = "^\\d{6}$";

	public static final int PAY_PASSWORD_LEN = 6;

	/** 钱包默认币种 */
	public static final String DEFAULT_CURRENCY = "USD";

	/** 业务类型：充值 */
	public static final String BIZ_RECHARGE = "RECHARGE";
	/** 业务类型：提现 */
	public static final String BIZ_WITHDRAW = "WITHDRAW";
	/** 业务类型：授权消费 */
	public static final String BIZ_AUTH = "AUTH";
	/** 业务类型：退款 */
	public static final String BIZ_REFUND = "REFUND";
	/** 业务类型：关卡 */
	public static final String BIZ_CLOSE = "CLOSE";
	/** 业务类型：开卡申请 */
	public static final String BIZ_APPLY = "APPLY";

	/** 默认卡标记 */
	public static final int CARD_DEFAULT_YES = 1;
	public static final int CARD_DEFAULT_NO = 0;

	/** 交易类型：充值 */
	public static final String TRANS_TOPUP = "TOPUP";

	/** 充值流水：处理中 */
	public static final int ORDER_STATE_PENDING = 0;
	public static final String ORDER_STATE_PENDING_NAME = "处理中";

	/** 证件类型：身份证（需正反面） */
	public static final int KYC_CERT_ID_CARD = 1;
	/** 证件类型：护照（仅正面） */
	public static final int KYC_CERT_PASSPORT = 2;
	/** 证件类型：驾照（需正反面） */
	public static final int KYC_CERT_DRIVER_LICENSE = 3;
}
