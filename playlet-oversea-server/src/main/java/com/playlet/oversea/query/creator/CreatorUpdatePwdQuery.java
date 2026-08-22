package com.playlet.oversea.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 作家修改密码请求。
 */
@Data
@ApiModel(value = "作家修改密码", description = "登录后校验原密码")
public class CreatorUpdatePwdQuery {

	@NotBlank
	@ApiModelProperty(name = "formerPassword", value = "原密码", required = true)
	private String formerPassword;

	@NotBlank
	@ApiModelProperty(name = "newPassword", value = "新密码", required = true)
	private String newPassword;
}
