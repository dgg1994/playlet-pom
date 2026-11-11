package com.playlet.internal.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 创作者提现申请。
 */
@Data
@TableName("creator_withdraw_apply")
@ApiModel(value = "创作者提现申请")
public class CreatorWithdrawApplyEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("creator_id")
	@ApiModelProperty(name = "creatorId", value = "creator_account.id")
	private Integer creatorId;

	@TableField("apply_no")
	@ApiModelProperty(name = "applyNo", value = "提现单号")
	private String applyNo;

	@TableField("amount")
	@ApiModelProperty(name = "amount", value = "提现金额（金币）")
	private Long amount;

	@TableField("fee")
	@ApiModelProperty(name = "fee", value = "手续费（金币）")
	private Long fee;

	@TableField("bank_name")
	@ApiModelProperty(name = "bankName", value = "收款银行/渠道")
	private String bankName;

	@TableField("bank_card_tail")
	@ApiModelProperty(name = "bankCardTail", value = "银行卡尾号")
	private String bankCardTail;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "0处理中 1已到账 2已失败")
	private Integer status;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "备注/失败原因")
	private String remark;

	@TableField("audit_admin_id")
	@ApiModelProperty(name = "auditAdminId", value = "处理人")
	private Integer auditAdminId;

	@TableField("audit_time")
	@ApiModelProperty(name = "auditTime", value = "处理时间")
	private Date auditTime;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
