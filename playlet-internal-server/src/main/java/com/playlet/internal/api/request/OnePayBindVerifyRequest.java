package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 绑定 OnePay 请求（透传三方校验接口）。
 */
@Data
@ApiModel("绑定OnePay请求")
public class OnePayBindVerifyRequest {

	@ApiModelProperty(value = "OnePay 账号", required = true)
	private String account;

	@ApiModelProperty(value = "登录邮箱验证码（sendEmailCode）", required = true)
	private String verificationCode;
}
