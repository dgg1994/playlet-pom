package com.playlet.internal.entity.coupon;

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
 * 用户持有优惠券。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_coupon")
@ApiModel(value = "用户优惠券", description = "用户持券与核销状态")
public class UserCouponEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户ID", dataType = "Integer")
	private Integer uid;

	@TableField("template_id")
	@ApiModelProperty(name = "templateId", value = "模板ID", dataType = "Integer")
	private Integer templateId;

	@TableField("coupon_code")
	@ApiModelProperty(name = "couponCode", value = "券编码快照", dataType = "String")
	private String couponCode;

	@TableField("coupon_name")
	@ApiModelProperty(name = "couponName", value = "券名称快照", dataType = "String")
	private String couponName;

	@TableField("coupon_type")
	@ApiModelProperty(name = "couponType", value = "类型快照", dataType = "String")
	private String couponType;

	@TableField("discount_rate")
	@ApiModelProperty(name = "discountRate", value = "折扣率快照", dataType = "BigDecimal")
	private BigDecimal discountRate;

	@TableField("threshold_amt")
	@ApiModelProperty(name = "thresholdAmt", value = "满减门槛快照", dataType = "BigDecimal")
	private BigDecimal thresholdAmt;

	@TableField("reduce_amt")
	@ApiModelProperty(name = "reduceAmt", value = "减免额快照", dataType = "BigDecimal")
	private BigDecimal reduceAmt;

	@TableField("currency")
	@ApiModelProperty(name = "currency", value = "单位快照", dataType = "String")
	private String currency;

	@TableField("scope")
	@ApiModelProperty(name = "scope", value = "适用范围快照", dataType = "String")
	private String scope;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "0未用 1已用 2过期 3作废", dataType = "Integer")
	private Integer status;

	@TableField("expire_time")
	@ApiModelProperty(name = "expireTime", value = "过期时间", dataType = "Date")
	private Date expireTime;

	@TableField("issue_batch_no")
	@ApiModelProperty(name = "issueBatchNo", value = "发放批次号", dataType = "String")
	private String issueBatchNo;

	@TableField("used_order_no")
	@ApiModelProperty(name = "usedOrderNo", value = "核销订单号", dataType = "String")
	private String usedOrderNo;

	@TableField("used_time")
	@ApiModelProperty(name = "usedTime", value = "核销时间", dataType = "Date")
	private Date usedTime;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "发放时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
