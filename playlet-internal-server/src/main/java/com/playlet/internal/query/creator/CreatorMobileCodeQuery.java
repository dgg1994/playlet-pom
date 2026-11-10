package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 手机验证码发送请求。
 */
@Data
@ApiModel(value = "发送手机验证码")
public class CreatorMobileCodeQuery {

	@NotBlank
	@ApiModelProperty(name = "mobilePrefix", value = "区号 如 +86", required = true)
	private String mobilePrefix;

	@NotBlank
	@ApiModelProperty(name = "mobileNumber", value = "手机号", required = true)
	private String mobileNumber;

	@ApiModelProperty(name = "scene", value = "1绑定新手机 2更换手机", example = "1")
	private Integer scene;
}
