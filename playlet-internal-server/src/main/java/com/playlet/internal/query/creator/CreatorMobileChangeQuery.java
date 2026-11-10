package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 更换 / 绑定手机号请求。
 */
@Data
@ApiModel(value = "更换手机号")
public class CreatorMobileChangeQuery {

	@NotBlank
	@ApiModelProperty(name = "mobilePrefix", value = "区号 如 +86", required = true)
	private String mobilePrefix;

	@NotBlank
	@ApiModelProperty(name = "mobileNumber", value = "手机号", required = true)
	private String mobileNumber;

	@NotBlank
	@ApiModelProperty(name = "code", value = "新手机验证码", required = true)
	private String code;

	@ApiModelProperty(name = "oldCode", value = "旧手机验证码（更换场景必填）")
	private String oldCode;
}
