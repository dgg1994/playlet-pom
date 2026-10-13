package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 删除弹幕入参。
 */
@Data
@ApiModel(value = "删除弹幕", description = "用户删除自己的弹幕")
public class DanmuDeleteRequest {

	@ApiModelProperty(value = "弹幕ID", required = true)
	private Long id;
}
