package com.playlet.internal.entity.drama;

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
@TableName("drama_asset_audit_step")
@ApiModel(value = "剧集审核步骤", description = "AI/A组/B组审核步骤")
public class DramaAssetAuditStepEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("asset_id")
	@ApiModelProperty("剧集ID")
	private Integer assetId;

	@TableField("drama_id")
	@ApiModelProperty("剧ID")
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
