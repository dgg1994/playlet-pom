package com.playlet.oversea.entity.drama;

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
@TableName("drama_audit_step")
@ApiModel(value = "短剧评审步骤", description = "剧级 AI/A组/B组审核步骤")
public class DramaAuditStepEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("drama_id")
	@ApiModelProperty("短剧ID")
	private Integer dramaId;

	@TableField("step_type")
	@ApiModelProperty("步骤类型 1AI 2A组 3B组")
	private Integer stepType;

	@TableField("status")
	@ApiModelProperty("步骤状态 0待审 1通过 2驳回")
	private Integer status;

	@TableField("handler_id")
	@ApiModelProperty("审核员ID")
	private Integer handlerId;

	@TableField("handle_remark")
	@ApiModelProperty("审核备注")
	private String handleRemark;

	@TableField("handle_time")
	@ApiModelProperty("审核时间")
	private Date handleTime;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
