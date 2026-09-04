package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 支付密码校验入参。
 */
@Data
@ApiModel(value = "支付密码校验", description = "校验当前用户支付密码是否正确")
public class WalletCheckPayPwdRequest {

	@ApiModelProperty(value = "支付密码，6位数字", required = true)
	@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	private String payPassword;
}
