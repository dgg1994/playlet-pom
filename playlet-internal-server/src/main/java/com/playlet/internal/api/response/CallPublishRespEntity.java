package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 发布征稿响应。
 */
@Data
@ApiModel(value = "发布征稿响应")
public class CallPublishRespEntity {

	@ApiModelProperty(name = "id", value = "新发布的征稿 id（初始为下架状态，审核通过后对外展示）")
	private Integer id;

	@ApiModelProperty(name = "msg", value = "提示信息")
	private String msg;
}
