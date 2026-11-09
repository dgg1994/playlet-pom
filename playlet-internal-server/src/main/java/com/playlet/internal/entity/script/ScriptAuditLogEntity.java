package com.playlet.internal.entity.script;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 剧本市场-审核/上下架操作日志。
 */
@Data
@TableName("script_audit_log")
@ApiModel(value = "剧本审核日志", description = "审核与上下架操作留痕")
public class ScriptAuditLogEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("script_id")
	@ApiModelProperty(name = "scriptId", value = "script.id")
	private Integer scriptId;

	@TableField("action_type")
	@ApiModelProperty(name = "actionType", value = "1审核 2上下架 3创建 4编辑")
	private Integer actionType;

	@TableField("from_audit_status")
	@ApiModelProperty(name = "fromAuditStatus", value = "变更前审核状态")
	private Integer fromAuditStatus;

	@TableField("to_audit_status")
	@ApiModelProperty(name = "toAuditStatus", value = "变更后审核状态")
	private Integer toAuditStatus;

	@TableField("from_shelf_status")
	@ApiModelProperty(name = "fromShelfStatus", value = "变更前上架状态")
	private Integer fromShelfStatus;

	@TableField("to_shelf_status")
	@ApiModelProperty(name = "toShelfStatus", value = "变更后上架状态")
	private Integer toShelfStatus;

	@TableField("operator_id")
	@ApiModelProperty(name = "operatorId", value = "操作人ID")
	private Integer operatorId;

	@TableField("operator_type")
	@ApiModelProperty(name = "operatorType", value = "1作家 2管理员")
	private Integer operatorType;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "备注/原因")
	private String remark;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "操作时间")
	private Date setTime;
}
