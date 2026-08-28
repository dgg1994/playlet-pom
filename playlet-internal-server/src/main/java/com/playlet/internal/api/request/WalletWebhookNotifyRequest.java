package com.playlet.internal.api.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * worldPay WebHook 回调入参（按 eventType 复用字段）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "钱包Webhook回调", description = "worldPay 统一回调体")
public class WalletWebhookNotifyRequest {

	@ApiModelProperty(value = "事件唯一键")
	private String eventId;

	@ApiModelProperty(value = "事件类型")
	private String eventType;

	@ApiModelProperty(value = "三方用户 uid")
	private Long uid;

	@ApiModelProperty(value = "三方卡 id")
	private Long userBankcardId;

	@ApiModelProperty(value = "卡号")
	private String cardNo;

	@ApiModelProperty(value = "充值金额")
	private String rechargeAmount;

	@ApiModelProperty(value = "三方充值单号")
	private String orderId;

	@ApiModelProperty(value = "KYC 审核状态")
	private String auditState;

	@ApiModelProperty(value = "KYC 审核备注")
	private String auditRemark;

	@ApiModelProperty(value = "3DS 验证类型")
	private String verificationType;

	@ApiModelProperty(value = "3DS 验证码")
	private String otp;

	@ApiModelProperty(value = "3DS 授权 id")
	private String authId;

	@ApiModelProperty(value = "交易金额")
	private String transactionAmount;

	@ApiModelProperty(value = "交易币种")
	private String transactionCurrency;

	@ApiModelProperty(value = "卡状态（cardActive/cardFreeze/cardClose 等）")
	private String status;

	@ApiModelProperty(value = "交易创建时间戳（毫秒）")
	private Long createAt;

	@ApiModelProperty(value = "币种")
	private String currency;

	@ApiModelProperty(value = "交易明细")
	private TransactionPayload transaction;

	@ApiModelProperty(value = "商户充值金额")
	private Double amount;

	@ApiModelProperty(value = "商户充值币种")
	private String curreny;

	@ApiModelProperty(value = "链上交易时间")
	private String txTime;

	@ApiModelProperty(value = "链上交易 hash")
	private String txHash;

	/**
	 * 卡交易明细。
	 */
	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TransactionPayload {

		private String localCurrency;

		private String localCurrencyAmt;

		private String merchantName;

		private Integer transStatus;

		private String respCodeDesc;

		private Integer transType;

		private String balance;

		private String feeAmount;

		private String feeCurrency;

		private String transactionId;

		private String originalTransactionId;
	}
}
