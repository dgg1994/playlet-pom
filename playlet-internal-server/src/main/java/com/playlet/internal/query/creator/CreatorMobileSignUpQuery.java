package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 作家手机号注册请求（手机号即登录账号）。
 */
@Data
@ApiModel(value = "作家手机号注册", description = "手机号 + 短信验证码注册，手机号即登录账号")
public class CreatorMobileSignUpQuery {

	@NotBlank
	@ApiModelProperty(name = "mobilePrefix", value = "国家区号，如 86 / +86", required = true)
	private String mobilePrefix;

	@NotBlank
	@ApiModelProperty(name = "mobileNumber", value = "手机号（不含区号）", required = true)
	private String mobileNumber;

	@NotBlank
	@ApiModelProperty(name = "smsCode", value = "短信验证码", required = true)
	private String smsCode;

	@NotBlank
	@ApiModelProperty(name = "userPassword", value = "登录密码", required = true)
	private String userPassword;

	@ApiModelProperty(name = "confirmPassword", value = "确认密码")
	private String confirmPassword;

	@ApiModelProperty(name = "identityType", value = "1个人创作者 2公司机构 3工作室团队")
	private Integer identityType;

	@ApiModelProperty(name = "nickname", value = "昵称，不填自动生成")
	private String nickname;
}
