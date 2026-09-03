package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 银行卡激活结果。
 */
@Data
@ApiModel(value = "银行卡激活结果", description = "POST /api/bankcard/active data")
public class ThirdBankcardActiveResp {

	@ApiModelProperty("卡号")
	private String cardNo;

	@ApiModelProperty("对方银行卡id")
	private Long userBankcardId;
}
