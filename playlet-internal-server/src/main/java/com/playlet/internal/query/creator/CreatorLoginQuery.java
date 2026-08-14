package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 作家登录请求。
 */
@Data
@ApiModel(value = "作家登录", description = "邮箱 + 密码")
public class CreatorLoginQuery {

	@NotBlank
	@ApiModelProperty(name = "userAccount", value = "登录邮箱", required = true)
	private String userAccount;

	@NotBlank
	@ApiModelProperty(name = "userPassword", value = "登录密码", required = true)
	private String userPassword;
}
