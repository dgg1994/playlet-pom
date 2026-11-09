package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 作家端收到的交易申请出参。
 */
@Data
@ApiModel(value = "交易申请行", description = "购买方信息脱敏")
public class ScriptTradeApplyRespEntity {

	@ApiModelProperty("申请ID")
	private Long applyId;

	@ApiModelProperty("申请编号")
	private String applyNo;

	@ApiModelProperty("剧本ID")
	private Integer scriptId;

	@ApiModelProperty("剧本名称快照")
	private String scriptName;

	@ApiModelProperty("购买方昵称")
	private String buyerName;

	@ApiModelProperty("联系人")
	private String contactName;

	@ApiModelProperty("联系电话（脱敏）")
	private String contactMobile;

	@ApiModelProperty("申请类型 1购买 2议价")
	private Integer applyType;

	@ApiModelProperty("申请类型文案")
	private String applyTypeText;

	@ApiModelProperty("报价（元）")
	private BigDecimal offerPrice;

	@ApiModelProperty("留言")
	private String message;

	@ApiModelProperty("申请状态")
	private Integer applyStatus;

	@ApiModelProperty("申请状态文案")
	private String applyStatusText;

	@ApiModelProperty("申请时间")
	private Date setTime;
}
