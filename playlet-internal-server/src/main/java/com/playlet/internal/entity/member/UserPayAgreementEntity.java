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
 * 用户三方代扣签约协议（连续包月）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_pay_agreement")
@ApiModel(value = "代扣签约协议", description = "连续包月签约记录")
public class UserPayAgreementEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户ID", dataType = "Integer")
	private Integer uid;

	@TableField("pay_channel")
	@ApiModelProperty(name = "payChannel", value = "签约渠道 WECHAT/ALIPAY", dataType = "String")
	private String payChannel;

	@TableField("pay_scene")
	@ApiModelProperty(name = "payScene", value = "签约场景 PC/H5/APP/MINI", dataType = "String")
	private String payScene;

	@TableField("product_id")
	@ApiModelProperty(name = "productId", value = "会员套餐ID", dataType = "Integer")
	private Integer productId;

	@TableField("external_agreement_no")
	@ApiModelProperty(name = "externalAgreementNo", value = "商户侧签约号（首期用首单号，保证幂等）", dataType = "String")
	private String externalAgreementNo;

	@TableField("agreement_no")
	@ApiModelProperty(name = "agreementNo", value = "三方协议号 支付宝 agreement_no / 微信 contract_id", dataType = "String")
	private String agreementNo;

	@TableField("third_user_id")
	@ApiModelProperty(name = "thirdUserId", value = "三方账号 openid / buyer_id", dataType = "String")
	private String thirdUserId;

	@TableField("plan_id")
	@ApiModelProperty(name = "planId", value = "微信签约计划号（商户平台创建）", dataType = "String")
	private String planId;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "0待签约 1已签约 2已解约 3失效", dataType = "Integer")
	private Integer status;

	@TableField("period_days")
	@ApiModelProperty(name = "periodDays", value = "每期天数", dataType = "Integer")
	private Integer periodDays;

	@TableField("renew_price")
	@ApiModelProperty(name = "renewPrice", value = "每期续费价(元)", dataType = "BigDecimal")
	private BigDecimal renewPrice;

	@TableField("sign_time")
	@ApiModelProperty(name = "signTime", value = "签约时间", dataType = "Date")
	private Date signTime;

	@TableField("cancel_time")
	@ApiModelProperty(name = "cancelTime", value = "解约时间", dataType = "Date")
	private Date cancelTime;

	@TableField("next_deduct_time")
	@ApiModelProperty(name = "nextDeductTime", value = "下次扣款时间", dataType = "Date")
	private Date nextDeductTime;

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
