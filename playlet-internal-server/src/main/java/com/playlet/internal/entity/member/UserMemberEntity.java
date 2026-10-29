package com.playlet.internal.entity.member;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 用户会员权益（全站一种会员）。
 */
@Data
@TableName("user_member")
@ApiModel(value = "用户会员", description = "用户会员到期与状态")
public class UserMemberEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户ID", dataType = "Integer")
	private Integer uid;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1有效 0关闭", dataType = "Integer")
	private Integer status;

	@TableField("expire_time")
	@ApiModelProperty(name = "expireTime", value = "到期时间", dataType = "Date")
	private Date expireTime;

	@TableField("last_order_no")
	@ApiModelProperty(name = "lastOrderNo", value = "最近开通订单号", dataType = "String")
	private String lastOrderNo;

	@TableField("auto_renew")
	@ApiModelProperty(name = "autoRenew", value = "是否开启连续包月自动续费 1是0否", dataType = "Integer")
	private Integer autoRenew;

	@TableField("agreement_id")
	@ApiModelProperty(name = "agreementId", value = "签约协议ID user_pay_agreement.id", dataType = "Integer")
	private Integer agreementId;

	@TableField("renew_product_id")
	@ApiModelProperty(name = "renewProductId", value = "自动续费套餐ID member_product.id", dataType = "Integer")
	private Integer renewProductId;

	@TableField("next_deduct_time")
	@ApiModelProperty(name = "nextDeductTime", value = "下次自动扣款时间", dataType = "Date")
	private Date nextDeductTime;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
