package com.playlet.oversea.entity.medal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_medal_unlock_log")
@ApiModel(value = "用户勋章流水", description = "勋章进度变更/解锁流水")
public class UserMedalUnlockLogEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Long")
	private Long id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户ID", dataType = "Long")
	private Long uid;

	@TableField("medal_id")
	@ApiModelProperty(name = "medalId", value = "勋章id", dataType = "Integer")
	private Integer medalId;

	@TableField("medal_code")
	@ApiModelProperty(name = "medalCode", value = "勋章业务码", dataType = "String")
	private String medalCode;

	@TableField("progress_before")
	@ApiModelProperty(name = "progressBefore", value = "变更前进度", dataType = "Integer")
	private Integer progressBefore;

	@TableField("progress_after")
	@ApiModelProperty(name = "progressAfter", value = "变更后进度", dataType = "Integer")
	private Integer progressAfter;

	@TableField("trigger_action")
	@ApiModelProperty(name = "triggerAction", value = "触发行为类型", dataType = "String")
	private String triggerAction;

	@TableField("trigger_ref")
	@ApiModelProperty(name = "triggerRef", value = "业务引用ID", dataType = "String")
	private String triggerRef;

	@TableField("unlock_flag")
	@ApiModelProperty(name = "unlockFlag", value = "本次是否解锁：0否 1是", dataType = "Integer")
	private Integer unlockFlag;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "发生时间", dataType = "Date")
	private Date setTime;
}
