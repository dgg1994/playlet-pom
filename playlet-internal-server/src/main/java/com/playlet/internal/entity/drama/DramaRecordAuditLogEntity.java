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
 * 短剧备案审核记录（时间线）。
 */
@Data
@TableName("drama_record_audit_log")
@ApiModel(value = "备案审核记录", description = "短剧备案审核操作日志")
public class DramaRecordAuditLogEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty("主键")
	private Long id;

	@TableField("drama_id")
	@ApiModelProperty("短剧ID")
	private Integer dramaId;

	@TableField("step_type")
	@ApiModelProperty("步骤类型 1初审 2复审")
	private Integer stepType;

	@TableField("action")
	@ApiModelProperty("操作：1通过 2驳回")
	private Integer action;

	@TableField("remark")
	@ApiModelProperty("审核意见")
	private String remark;

	@TableField("handler_id")
	@ApiModelProperty("审核员ID")
	private Integer handlerId;

	@TableField("handler_name")
	@ApiModelProperty("审核员名称")
	private String handlerName;

	@TableField("setTime")
	private Date setTime;
}
