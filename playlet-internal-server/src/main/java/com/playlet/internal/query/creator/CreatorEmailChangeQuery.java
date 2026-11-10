package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 更换邮箱请求。
 */
@Data
@ApiModel(value = "更换邮箱")
public class CreatorEmailChangeQuery {

	@NotBlank
	@ApiModelProperty(name = "newEmail", value = "新邮箱", required = true)
	private String newEmail;

	@NotBlank
	@ApiModelProperty(name = "emailCode", value = "邮箱验证码", required = true)
	private String emailCode;

	@NotBlank
	@ApiModelProperty(name = "password", value = "当前登录密码", required = true)
	private String password;
}
