package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理端冻结/解冻入参。
 */
@Data
@ApiModel(value = "用户冻结", description = "冻结或解冻账号")
public class AppUserFreezeQuery {

	@ApiModelProperty(value = "用户ID", required = true)
	private Integer uid;

	@ApiModelProperty("原因")
	private String reason;
}
