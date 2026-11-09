package com.playlet.internal.query.script;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * C 端购买/议价申请入参。
 */
@Data
@ApiModel(value = "剧本交易申请", description = "购买或议价")
public class ScriptTradeApplyQuery {

	@NotNull(message = "scriptId 不能为空")
	@ApiModelProperty(name = "scriptId", value = "剧本ID", required = true)
	private Integer scriptId;

	@NotNull(message = "applyType 不能为空")
	@ApiModelProperty(name = "applyType", value = "1购买 2议价", required = true)
	private Integer applyType;

	@ApiModelProperty(name = "offerPrice", value = "议价金额（元），议价必填")
	private BigDecimal offerPrice;

	@NotBlank(message = "contactName 不能为空")
	@Size(max = 64, message = "contactName 过长")
	@ApiModelProperty(name = "contactName", value = "联系人", required = true)
	private String contactName;

	@NotBlank(message = "contactMobile 不能为空")
	@Size(max = 32, message = "contactMobile 过长")
	@ApiModelProperty(name = "contactMobile", value = "联系电话", required = true)
	private String contactMobile;

	@Size(max = 512, message = "message 过长")
	@ApiModelProperty(name = "message", value = "留言/议价说明")
	private String message;
}
