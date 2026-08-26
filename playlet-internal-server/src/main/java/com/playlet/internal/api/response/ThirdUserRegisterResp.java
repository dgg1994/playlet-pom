package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * worldPay 用户注册响应 data。
 */
@Data
@ApiModel(value = "三方用户注册结果", description = "data.uid")
public class ThirdUserRegisterResp {

	@ApiModelProperty("worldPay 用户 uid")
	private Long uid;
}
