package com.playlet.internal.query.script;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 作家端处理交易申请入参。
 */
@Data
@ApiModel(value = "处理交易申请", description = "同意或拒绝")
public class ScriptApplyHandleQuery {

	@NotNull(message = "applyId 不能为空")
	@ApiModelProperty(name = "applyId", value = "申请ID", required = true)
	private Long applyId;

	@NotNull(message = "action 不能为空")
	@ApiModelProperty(name = "action", value = "1同意 2拒绝", required = true)
	private Integer action;

	@ApiModelProperty(name = "reply", value = "回复")
	private String reply;
}
