package com.playlet.internal.api.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * worldPay 用户注册请求体（仅业务字段，鉴权走 Header）。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "三方用户注册", description = "POST /api/user/register body")
public class RegisterRequest {

	@ApiModelProperty(value = "用户邮箱", required = true)
	private String email;

	@ApiModelProperty(value = "用户手机号")
	private String tel;
}
