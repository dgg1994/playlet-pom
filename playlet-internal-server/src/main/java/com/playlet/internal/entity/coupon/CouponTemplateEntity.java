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
 * 优惠券模板。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon_template")
@ApiModel(value = "优惠券模板", description = "优惠券定义与发放统计")
public class CouponTemplateEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("coupon_code")
	@ApiModelProperty(name = "couponCode", value = "券编码", dataType = "String")
	private String couponCode;

	@TableField("coupon_name")
	@ApiModelProperty(name = "couponName", value = "券名称", dataType = "String")
	private String couponName;

	@TableField("coupon_type")
	@ApiModelProperty(name = "couponType", value = "PERCENT/FULL_REDUCE/FIXED", dataType = "String")
	private String couponType;

	@TableField("discount_rate")
	@ApiModelProperty(name = "discountRate", value = "折扣率，如 0.8 表示 8 折", dataType = "BigDecimal")
	private BigDecimal discountRate;

	@TableField("threshold_amt")
	@ApiModelProperty(name = "thresholdAmt", value = "满减门槛", dataType = "BigDecimal")
	private BigDecimal thresholdAmt;

	@TableField("reduce_amt")
	@ApiModelProperty(name = "reduceAmt", value = "减免额", dataType = "BigDecimal")
	private BigDecimal reduceAmt;

	@TableField("currency")
	@ApiModelProperty(name = "currency", value = "YUAN/COIN", dataType = "String")
	private String currency;

	@TableField("scope")
	@ApiModelProperty(name = "scope", value = "MEMBER/COIN_CONSUME/RECHARGE/ALL", dataType = "String")
	private String scope;

	@TableField("valid_days")
	@ApiModelProperty(name = "validDays", value = "领取后有效天数", dataType = "Integer")
	private Integer validDays;

	@TableField("valid_start")
	@ApiModelProperty(name = "validStart", value = "固定有效期起", dataType = "Date")
	private Date validStart;

	@TableField("valid_end")
	@ApiModelProperty(name = "validEnd", value = "固定有效期止", dataType = "Date")
	private Date validEnd;

	@TableField("total_limit")
	@ApiModelProperty(name = "totalLimit", value = "总发放上限；空不限", dataType = "Integer")
	private Integer totalLimit;

	@TableField("per_user_limit")
	@ApiModelProperty(name = "perUserLimit", value = "每用户限领", dataType = "Integer")
	private Integer perUserLimit;

	@TableField("issued_count")
	@ApiModelProperty(name = "issuedCount", value = "已发放数量", dataType = "Integer")
	private Integer issuedCount;

	@TableField("used_count")
	@ApiModelProperty(name = "usedCount", value = "已核销数量", dataType = "Integer")
	private Integer usedCount;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1启用 0停用", dataType = "Integer")
	private Integer status;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "备注", dataType = "String")
	private String remark;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;

	/** 列表展示用面额文案（非表字段） */
	@TableField(exist = false)
	@ApiModelProperty(name = "faceValueText", value = "面额展示文案", dataType = "String")
	private String faceValueText;
}
