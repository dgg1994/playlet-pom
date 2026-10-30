package com.playlet.internal.entity.drama;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 短剧备案审核步骤（初审/复审）。
 */
@Data
@TableName("drama_record_audit_step")
@ApiModel(value = "备案审核步骤", description = "短剧备案审核步骤：初审=1 复审=2")
public class DramaRecordAuditStepEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty("主键")
	private Long id;

	@TableField("drama_id")
	@ApiModelProperty("短剧ID")
	private Integer dramaId;

	@TableField("step_type")
	@ApiModelProperty("步骤类型 1初审 2复审")
	private Integer stepType;

	@TableField("status")
	@ApiModelProperty("步骤状态 0待审 1通过 2驳回")
	private Integer status;

	@TableField("handler_id")
	@ApiModelProperty("审核员ID")
	private Integer handlerId;

	@TableField("handler_name")
	@ApiModelProperty("审核员名称")
	private String handlerName;

	@TableField("handle_remark")
	@ApiModelProperty("审核备注/驳回原因")
	private String handleRemark;

	@TableField("handle_time")
	@ApiModelProperty("审核时间")
	private Date handleTime;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
