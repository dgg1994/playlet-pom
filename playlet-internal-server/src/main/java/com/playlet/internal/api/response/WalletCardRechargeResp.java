package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 银行卡充值结果。
 */
@Data
@ApiModel("银行卡充值结果")
public class WalletCardRechargeResp {

	@ApiModelProperty("商户订单号")
	private String requestOrderId;

	@ApiModelProperty("充值金额")
	private BigDecimal amount;

	@ApiModelProperty("扣款后钱包可用余额")
	private BigDecimal availableBalance;

	@ApiModelProperty("是否幂等重复请求（true 表示未再次扣款）")
	private Boolean idempotent;
}
