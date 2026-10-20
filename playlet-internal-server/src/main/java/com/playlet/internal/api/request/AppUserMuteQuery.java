package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理端禁言/解禁入参。
 */
@Data
@ApiModel(value = "用户禁言", description = "临时禁言或解除禁言")
public class AppUserMuteQuery {

	@ApiModelProperty(value = "用户ID", required = true)
	private Integer uid;

	@ApiModelProperty(value = "禁言天数；解禁时可空。>=1")
	private Integer muteDays;

	@ApiModelProperty("原因")
	private String reason;
}
