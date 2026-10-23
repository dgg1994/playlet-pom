package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端退款申请列表项。
 */
@Data
@ApiModel(value = "退款申请项", description = "退款管理列表行")
public class RefundApplyItemEntity {

	@ApiModelProperty("申请主键")
	private Integer id;

	@ApiModelProperty("申请单号")
	private String applyNo;

	@ApiModelProperty("原订单号")
	private String orderNo;

	@ApiModelProperty("MEMBER / EPISODE")
	private String productType;

	@ApiModelProperty("用户ID")
	private Integer uid;

	@ApiModelProperty("用户昵称")
	private String nickname;

	@ApiModelProperty("退款原因")
	private String refundReason;

	@ApiModelProperty("退款金额(元)")
	private BigDecimal refundAmount;

	@ApiModelProperty("申请时间")
	private Date setTime;

	@ApiModelProperty("0待处理 1已退款 2已拒绝")
	private Integer status;

	@ApiModelProperty("处理时间")
	private Date handleTime;

	@ApiModelProperty("处理备注")
	private String handleRemark;
}
