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
 * 会员开通/续费订单。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_order")
@ApiModel(value = "会员订单", description = "会员开通续费订单")
public class MemberOrderEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("order_no")
	@ApiModelProperty(name = "orderNo", value = "业务订单号", dataType = "String")
	private String orderNo;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户ID", dataType = "Integer")
	private Integer uid;

	@TableField("product_id")
	@ApiModelProperty(name = "productId", value = "套餐ID", dataType = "Integer")
	private Integer productId;

	@TableField("product_code")
	@ApiModelProperty(name = "productCode", value = "套餐编码快照", dataType = "String")
	private String productCode;

	@TableField("duration_days")
	@ApiModelProperty(name = "durationDays", value = "开通天数快照", dataType = "Integer")
	private Integer durationDays;

	@TableField("pay_type")
	@ApiModelProperty(name = "payType", value = "COIN金币 / CASH现金", dataType = "String")
	private String payType;

	@TableField("pay_amount")
	@ApiModelProperty(name = "payAmount", value = "现金实付(元)；金币支付可为0", dataType = "BigDecimal")
	private BigDecimal payAmount;

	@TableField("pay_coin")
	@ApiModelProperty(name = "payCoin", value = "金币实付数量", dataType = "Integer")
	private Integer payCoin;

	@TableField("user_coupon_id")
	@ApiModelProperty(name = "userCouponId", value = "使用的持券ID", dataType = "Integer")
	private Integer userCouponId;

	@TableField("discount_amt")
	@ApiModelProperty(name = "discountAmt", value = "优惠减免（元或金币，与 payType 一致）", dataType = "BigDecimal")
	private BigDecimal discountAmt;

	@TableField("pay_channel")
	@ApiModelProperty(name = "payChannel", value = "现金渠道 WECHAT/ALIPAY/BANK；金币支付可空", dataType = "String")
	private String payChannel;

	@TableField("pay_status")
	@ApiModelProperty(name = "payStatus", value = "0待支付 1已支付 2已关闭 3已退款", dataType = "Integer")
	private Integer payStatus;

	@TableField("pay_time")
	@ApiModelProperty(name = "payTime", value = "支付成功时间", dataType = "Date")
	private Date payTime;

	@TableField("third_trade_no")
	@ApiModelProperty(name = "thirdTradeNo", value = "三方支付单号", dataType = "String")
	private String thirdTradeNo;

	@TableField("expire_before")
	@ApiModelProperty(name = "expireBefore", value = "开通前到期时间", dataType = "Date")
	private Date expireBefore;

	@TableField("expire_after")
	@ApiModelProperty(name = "expireAfter", value = "开通后到期时间", dataType = "Date")
	private Date expireAfter;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "备注", dataType = "String")
	private String remark;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
