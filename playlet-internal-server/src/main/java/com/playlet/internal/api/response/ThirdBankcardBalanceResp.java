package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 银行卡余额。
 */
@Data
@ApiModel(value = "银行卡余额", description = "POST /api/bankcard/getBalance data")
public class ThirdBankcardBalanceResp {

	@ApiModelProperty("余额")
	private String balance;

	@ApiModelProperty("币种")
	private String currency;

	@ApiModelProperty("符号")
	private String symbol;

	@ApiModelProperty("卡限额")
	private String cardLimit;
}
