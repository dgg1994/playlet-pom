package com.playlet.oversea.constants;

/**
 * 钱包 WebHook 常量。
 */
public final class WalletWebhookConstants {

	private WalletWebhookConstants() {
	}

	/** 成功响应文案（文档约定） */
	public static final String ACK_MSG = "Received successfully";

	/** 事件：KYC 状态变更 */
	public static final String EVENT_KYC_STATUS_CHANGE = "kycStatusChange";
	/** 事件：3DS 授权 */
	public static final String EVENT_3DS = "3ds";
	/** 事件：卡状态变更 */
	public static final String EVENT_CARD_STATUS_CHANGE = "cardStatusChange";
	/** 事件：充值结果 */
	public static final String EVENT_CARD_RECHARGE_RESULT = "cardRechargeResult";
	/** 事件：卡交易 */
	public static final String EVENT_TRANSACTION_CREATED = "transactionCreated";
	/** 事件：商户充值 */
	public static final String EVENT_MERCHANT_RECHARGE = "merchantRecharge";

	/** cardStatusChange：卡片激活 */
	public static final String STATUS_CARD_ACTIVE = "cardActive";
	/** cardStatusChange：卡片冻结 */
	public static final String STATUS_CARD_FREEZE = "cardFreeze";
	/** cardStatusChange：卡片关闭 */
	public static final String STATUS_CARD_CLOSE = "cardClose";

	/** 虚拟卡激活确认：轮询三方卡信息最大重试次数（对齐 onetoken） */
	public static final int CARD_ACTIVE_CONFIRM_MAX_RETRIES = 5;
	/** 虚拟卡激活确认：轮询间隔毫秒 */
	public static final long CARD_ACTIVE_CONFIRM_RETRY_INTERVAL_MS = 3000L;

	/** 处理状态：待处理 */
	public static final int PROCESS_PENDING = 0;
	/** 处理状态：成功 */
	public static final int PROCESS_SUCCESS = 1;
	/** 处理状态：失败 */
	public static final int PROCESS_FAILED = 2;
	/** 处理状态：忽略 */
	public static final int PROCESS_IGNORED = 3;

	/** 提现打款网关：钱包 U 卡 */
	public static final String GATEWAY_WALLET = "WALLET";
}
