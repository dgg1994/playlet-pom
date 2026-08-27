package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 钱包支付密码绑定入参。
 */
@Data
@ApiModel(value = "钱包支付密码绑定", description = "首次设置支付密码")
public class WalletBindPayPwdRequest {

	@ApiModelProperty(value = "支付密码，6位数字", required = true)
	@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	private String payPassword;

	@ApiModelProperty(value = "确认支付密码", required = true)
	@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	private String confirmPayPassword;
}
