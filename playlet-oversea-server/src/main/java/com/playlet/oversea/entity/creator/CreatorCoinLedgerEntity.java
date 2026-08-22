package com.playlet.oversea.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 作家金币流水。
 */
@Data
@TableName("creator_coin_ledger")
@ApiModel(value = "作家金币流水", description = "收益入账 / 提现冻结 / 退回 / 调账")
public class CreatorCoinLedgerEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("creator_id")
	@ApiModelProperty(name = "creatorId", value = "creator_account.id")
	private Integer creatorId;

	@TableField("change_amt")
	@ApiModelProperty(name = "changeAmt", value = "变动金币，正加负减")
	private Long changeAmt;

	@TableField("balance_before")
	@ApiModelProperty(name = "balanceBefore", value = "变动前余额")
	private Long balanceBefore;

	@TableField("balance_after")
	@ApiModelProperty(name = "balanceAfter", value = "变动后余额")
	private Long balanceAfter;

	@TableField("frozen_before")
	@ApiModelProperty(name = "frozenBefore", value = "变动前冻结")
	private Long frozenBefore;

	@TableField("frozen_after")
	@ApiModelProperty(name = "frozenAfter", value = "变动后冻结")
	private Long frozenAfter;

	@TableField("biz_type")
	@ApiModelProperty(name = "bizType", value = "PLAY_INCOME / WITHDRAW / WITHDRAW_REFUND / SYSTEM")
	private String bizType;

	@TableField("biz_id")
	@ApiModelProperty(name = "bizId", value = "幂等键")
	private String bizId;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "备注")
	private String remark;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
