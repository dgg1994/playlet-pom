package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创建交易申请响应。
 */
@Data
@ApiModel(value = "创建交易申请响应")
public class ScriptApplyCreateRespEntity {

	@ApiModelProperty("申请ID")
	private Long applyId;

	@ApiModelProperty("申请编号")
	private String applyNo;

	@ApiModelProperty("申请状态")
	private Integer applyStatus;

	@ApiModelProperty("申请状态文案")
	private String applyStatusText;
}
