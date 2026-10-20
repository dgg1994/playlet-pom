package com.playlet.internal.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * C 端用户账号状态流水。
 */
@Data
@TableName("app_user_status_log")
@ApiModel(value = "用户状态流水", description = "禁言/冻结等账号状态变更记录")
public class AppUserStatusLogEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty("主键")
	private Long id;

	@TableField("uid")
	@ApiModelProperty("用户ID")
	private Integer uid;

	@TableField("action")
	@ApiModelProperty("动作码 REGISTER/MUTE/UNMUTE/FREEZE/UNFREEZE/NORMAL")
	private String action;

	@TableField("from_state")
	@ApiModelProperty("变更前 user_state")
	private Integer fromState;

	@TableField("to_state")
	@ApiModelProperty("变更后 user_state")
	private Integer toState;

	@TableField("mute_until")
	@ApiModelProperty("禁言截止")
	private Date muteUntil;

	@TableField("reason")
	@ApiModelProperty("原因")
	private String reason;

	@TableField("operator_id")
	@ApiModelProperty("管理员ID")
	private Integer operatorId;

	@TableField("operator_name")
	@ApiModelProperty("操作人名称")
	private String operatorName;

	@TableField("setTime")
	@ApiModelProperty("记录时间")
	private Date setTime;

	@TableField(exist = false)
	@ApiModelProperty("动作展示文案")
	private String actionLabel;
}
