package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 作家找回密码请求。
 */
@Data
@ApiModel(value = "作家找回密码", description = "邮箱验证码重置密码")
public class CreatorForgetPwdQuery {

	@NotBlank
	@ApiModelProperty(name = "userAccount", value = "登录邮箱", required = true)
	private String userAccount;

	@NotBlank
	@ApiModelProperty(name = "emailCode", value = "邮箱验证码", required = true)
	private String emailCode;

	@NotBlank
	@ApiModelProperty(name = "newPassword", value = "新密码", required = true)
	private String newPassword;
}
