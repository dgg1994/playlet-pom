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
 * 创作者注销申请（含冷静期）。
 */
@Data
@TableName("creator_cancel_apply")
@ApiModel(value = "创作者注销申请")
public class CreatorCancelApplyEntity {

	@TableId(type = IdType.AUTO)
	private Integer id;

	@TableField("creator_id")
	private Integer creatorId;

	@TableField("reason")
	@ApiModelProperty(name = "reason", value = "注销原因")
	private String reason;

	/** 1冷静期 2已注销 3已撤销 4已驳回 */
	@TableField("status")
	private Integer status;

	@TableField("reject_reason")
	private String rejectReason;

	@TableField("apply_time")
	private Date applyTime;

	@TableField("cancel_deadline")
	private Date cancelDeadline;

	@TableField("handle_time")
	private Date handleTime;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
