package com.playlet.internal.query.script;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 剧本上架/下架入参。
 */
@Data
@ApiModel(value = "剧本上下架", description = "作家端上架/下架")
public class ScriptShelfQuery {

	@NotNull(message = "id 不能为空")
	@ApiModelProperty(name = "id", value = "剧本ID", required = true)
	private Integer id;

	@NotNull(message = "shelfStatus 不能为空")
	@ApiModelProperty(name = "shelfStatus", value = "0下架 1上架", required = true)
	private Integer shelfStatus;

	@ApiModelProperty(name = "reason", value = "下架原因")
	private String reason;
}
