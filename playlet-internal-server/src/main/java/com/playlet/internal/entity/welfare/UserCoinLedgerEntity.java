package com.playlet.internal.entity.welfare;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_coin_ledger")
@ApiModel(value = "用户金币流水", description = "金币加减流水")
public class UserCoinLedgerEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Long")
	private Long id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户uid", dataType = "String")
	private String uid;

	@TableField("change_amt")
	@ApiModelProperty(name = "changeAmt", value = "变动金额，正加负减", dataType = "Integer")
	private Integer changeAmt;

	@TableField("balance_before")
	@ApiModelProperty(name = "balanceBefore", value = "变动前余额", dataType = "Long")
	private Long balanceBefore;

	@TableField("balance_after")
	@ApiModelProperty(name = "balanceAfter", value = "变动后余额", dataType = "Long")
	private Long balanceAfter;

	@TableField("biz_type")
	@ApiModelProperty(name = "bizType", value = "业务类型", dataType = "String")
	private String bizType;

	@TableField("biz_id")
	@ApiModelProperty(name = "bizId", value = "幂等业务键", dataType = "String")
	private String bizId;

	@TableField("task_code")
	@ApiModelProperty(name = "taskCode", value = "关联任务编码", dataType = "String")
	private String taskCode;

	@TableField("ad_boost_flag")
	@ApiModelProperty(name = "adBoostFlag", value = "0否 1看广告加赠", dataType = "Integer")
	private Integer adBoostFlag;

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
