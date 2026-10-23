package com.playlet.internal.entity.member;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 退款申请单。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("refund_apply")
@ApiModel(value = "退款申请", description = "用户退款申请与审核")
public class RefundApplyEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty("主键")
	private Integer id;

	@TableField("apply_no")
	@ApiModelProperty("退款申请单号")
	private String applyNo;

	@TableField("order_no")
	@ApiModelProperty("原订单号")
	private String orderNo;

	@TableField("product_type")
	@ApiModelProperty("MEMBER / EPISODE")
	private String productType;

	@TableField("uid")
	@ApiModelProperty("申请人用户ID")
	private Integer uid;

	@TableField("refund_amount")
	@ApiModelProperty("退款金额(元)")
	private BigDecimal refundAmount;

	@TableField("refund_reason")
	@ApiModelProperty("退款原因")
	private String refundReason;

	@TableField("status")
	@ApiModelProperty("0待处理 1已退款 2已拒绝")
	private Integer status;

	@TableField("handle_admin_id")
	@ApiModelProperty("处理人后台账号ID")
	private Integer handleAdminId;

	@TableField("handle_time")
	@ApiModelProperty("处理时间")
	private Date handleTime;

	@TableField("handle_remark")
	@ApiModelProperty("处理备注")
	private String handleRemark;

	@TableField("setTime")
	@ApiModelProperty("申请时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty("更新时间")
	private Date gmtModified;

	@TableField(exist = false)
	@ApiModelProperty("用户昵称（列表联查）")
	private String nickname;

	@TableField(exist = false)
	@ApiModelProperty("列表筛选关键词：订单号/昵称/用户ID")
	private String keyword;
}
