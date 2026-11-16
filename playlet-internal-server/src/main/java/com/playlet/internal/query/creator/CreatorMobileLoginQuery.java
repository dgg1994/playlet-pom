package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 作家手机号登录请求：密码与短信验证码二选一。
 */
@Data
@ApiModel(value = "作家手机号登录", description = "手机号 + 密码，或手机号 + 短信验证码")
public class CreatorMobileLoginQuery {

	@NotBlank
	@ApiModelProperty(name = "mobilePrefix", value = "国家区号，如 86 / +86", required = true)
	private String mobilePrefix;

	@NotBlank
	@ApiModelProperty(name = "mobileNumber", value = "手机号（不含区号）", required = true)
	private String mobileNumber;

	@ApiModelProperty(name = "userPassword", value = "登录密码，与 smsCode 二选一")
	private String userPassword;

	@ApiModelProperty(name = "smsCode", value = "短信验证码，传了则走验证码登录")
	private String smsCode;
}
