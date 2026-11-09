package com.playlet.internal.query.script;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 仅含剧本ID的通用入参。
 */
@Data
@ApiModel(value = "剧本ID入参", description = "删除等仅需ID的场景")
public class ScriptIdQuery {

	@NotNull(message = "id 不能为空")
	@ApiModelProperty(name = "id", value = "剧本ID", required = true)
	private Integer id;
}
