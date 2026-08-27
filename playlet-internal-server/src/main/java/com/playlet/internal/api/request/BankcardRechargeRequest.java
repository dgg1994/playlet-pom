package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 银行卡充值入参。
 */
@Data
@ApiModel(value = "银行卡充值", description = "POST /api/bankcard/recharge")
public class BankcardRechargeRequest {

	@ApiModelProperty(value = "银行卡id", required = true)
	private Long userBankcardId;

	@ApiModelProperty(value = "充值金额", required = true)
	private Integer amount;

	@ApiModelProperty(value = "商户订单号（幂等）", required = true)
	private String requestOrderId;
}
