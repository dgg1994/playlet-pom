package com.playlet.oversea.constants;

import java.math.BigDecimal;

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

	/** 内部转账默认费率（未配置时） */
	public static final BigDecimal ZERO_RATE = BigDecimal.ZERO;

	/** 内部转账订单号前缀 */
	public static final String TRANSFER_ORDER_PREFIX = "TF";

	/** 开卡申请幂等单号前缀 */
	public static final String REQUEST_ORDER_PREFIX_CARD_APPLY = "CA";

	/** 银行卡充值幂等单号前缀 */
	public static final String REQUEST_ORDER_PREFIX_CARD_RECHARGE = "CR";

	/** 开卡首充幂等单号前缀（无申请单号时） */
	public static final String REQUEST_ORDER_PREFIX_FIRST_TOPUP = "FT";

	/** 开卡解冻账变 outOrderNo 后缀（与申请单 id 拼接，幂等） */
	public static final String WALLET_LOG_OUT_ORDER_THAW_SUFFIX = "-THAW";

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
	/** 业务类型：短剧金币提现入账钱包余额（非 U 卡扣款） */
	public static final String BIZ_COIN_TO_WALLET = "COIN_TO_WALLET";

	/** 交易类型：充值 */
	public static final String TRANS_TOPUP = "TOPUP";
	/** 交易类型：金币提现入账钱包 */
	public static final String TRANS_COIN_TO_WALLET = "COIN_TO_WALLET";

	/**
	 * 金币提现入账钱包时无实体卡，wallet_card_transaction.user_bankcard_id 占位值。
	 */
	public static final long WALLET_BALANCE_BANKCARD_PLACEHOLDER = 0L;

	/** 默认卡标记 */
	public static final int CARD_DEFAULT_YES = 1;
	public static final int CARD_DEFAULT_NO = 0;

	/** 证件类型：身份证（需正反面） */
	public static final int KYC_CERT_ID_CARD = 1;
	/** 证件类型：护照（仅正面） */
	public static final int KYC_CERT_PASSPORT = 2;
	/** 证件类型：驾照（需正反面） */
	public static final int KYC_CERT_DRIVER_LICENSE = 3;

	/** 卡性质：虚拟卡 */
	public static final String BANKCARD_NATURE_VIRTUAL = "VIRTUAL";
	/** 卡性质：实体卡 */
	public static final String BANKCARD_NATURE_PHYSICAL = "PHYSICAL";

	/** 开卡充值方式：钱包余额 */
	public static final int TOPUP_TYPE_WALLET = 1;
	/** 开卡充值方式：银行卡 */
	public static final int TOPUP_TYPE_BANKCARD = 2;

	/** KYC 证件文件：正面 */
	public static final int KYC_DOC_FRONT = 1;
	/** KYC 证件文件：反面 */
	public static final int KYC_DOC_BACK = 2;
	/** KYC 证件文件：手持/自拍 */
	public static final int KYC_DOC_HANDHELD = 3;

	/** 开卡 KYC 证件类型文案：护照 */
	public static final String PAPERWORK_PASSPORT = "PASSPORT";
	/** 开卡 KYC 证件类型文案：身份证 */
	public static final String PAPERWORK_NATIONAL_ID = "NATIONAL_ID";

	/** 三方 KYC 文件上传表单字段名 */
	public static final String KYC_UPLOAD_FIELD_ID_CARD = "idCard";

	/** 管理端上下架：1 上架 / 2 下架 */
	public static final int ADMIN_CARD_STATE_ON = 1;
	public static final int ADMIN_CARD_STATE_OFF = 2;

	/** C 端 app 用户类型 */
	public static final int USER_TYPE_APP = 1;

	/** 人工充值订单号前缀 */
	public static final String MANUAL_TOPUP_ORDER_PREFIX = "MT";

	/** KYC 上传允许的文件后缀（小写，含点） */
	public static final String[] KYC_UPLOAD_ALLOWED_SUFFIXES = {".png", ".pdf", ".jpg", ".jpeg"};
}
