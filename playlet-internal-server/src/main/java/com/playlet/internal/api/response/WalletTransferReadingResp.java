package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 内部转账试算结果。
 */
@Data
@ApiModel(value = "内部转账试算", description = "手续费与实际到账预览")
public class WalletTransferReadingResp {

	@ApiModelProperty(name = "sendRates", value = "费率")
	private BigDecimal sendRates;

	@ApiModelProperty(name = "handlingFee", value = "手续费")
	private BigDecimal handlingFee;

	@ApiModelProperty(name = "actualMoney", value = "实际到账")
	private BigDecimal actualMoney;
}
