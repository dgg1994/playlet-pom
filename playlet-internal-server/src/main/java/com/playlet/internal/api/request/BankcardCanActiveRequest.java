package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 银行卡是否可激活入参。
 */
@Data
@ApiModel(value = "银行卡是否可激活", description = "POST /api/bankcard/get/canActive")
public class BankcardCanActiveRequest {

	@ApiModelProperty(value = "银行卡号", required = true)
	private String cardNo;

	@ApiModelProperty(value = "激活码", required = true)
	private String verifyCode;
}
