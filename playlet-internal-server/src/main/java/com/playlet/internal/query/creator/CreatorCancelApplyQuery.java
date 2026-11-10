package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 注销申请。
 */
@Data
@ApiModel(value = "创作者注销申请")
public class CreatorCancelApplyQuery {

	@NotBlank
	@ApiModelProperty(name = "reason", value = "注销原因", required = true)
	private String reason;

	@NotBlank
	@ApiModelProperty(name = "password", value = "当前登录密码", required = true)
	private String password;
}
