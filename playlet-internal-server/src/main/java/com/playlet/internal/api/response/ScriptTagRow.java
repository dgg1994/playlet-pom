package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 剧本标签批量查询行。
 */
@Data
@ApiModel(value = "剧本标签行")
public class ScriptTagRow {

	private Integer scriptId;
	private String tagName;
}
