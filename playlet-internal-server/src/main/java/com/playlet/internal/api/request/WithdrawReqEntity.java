package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 提现入参：金币按 withdraw_config 换算为 U 并入账钱包余额。
 */
@Data
@ApiModel("提现请求")
public class WithdrawReqEntity {

	@NotNull(message = "提现金币不能为空")
	@Min(value = 1, message = "提现金币须大于0")
	@ApiModelProperty(value = "提现金币数量", required = true)
	private Integer points;

	@ApiModelProperty(value = "币种编码，如 USDT；不传则取启用配置首条")
	private String assetCode;

	@ApiModelProperty(value = "网络，如 TRC20；不传则取启用配置首条")
	private String network;
}
