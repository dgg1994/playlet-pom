package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 银行卡充值结果（对齐 onetoken topUp 回执字段）。
 */
@Data
@ApiModel("银行卡充值结果")
public class WalletCardRechargeResp {

	@ApiModelProperty("商户订单号")
	private String requestOrderId;

	@ApiModelProperty("充值金额（同 targetAmount）")
	private BigDecimal amount;

	@ApiModelProperty("卡到账金额")
	private BigDecimal targetAmount;

	@ApiModelProperty("手续费")
	private BigDecimal handlingFees;

	@ApiModelProperty("充值方式：1 钱包 2 银行卡")
	private Integer payType;

	@ApiModelProperty("扣款后钱包可用余额")
	private BigDecimal availableBalance;

	@ApiModelProperty("是否幂等重复请求（true 表示未再次扣款）")
	private Boolean idempotent;
}
