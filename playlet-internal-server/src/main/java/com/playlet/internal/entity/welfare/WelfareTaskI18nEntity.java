package com.playlet.internal.entity.welfare;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("welfare_task_i18n")
@ApiModel(value = "福利任务多语言", description = "任务名称/说明多语言")
public class WelfareTaskI18nEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("task_id")
	@ApiModelProperty(name = "taskId", value = "任务id", dataType = "Integer")
	private Integer taskId;

	@TableField("langue")
	@ApiModelProperty(name = "langue", value = "语言", dataType = "String")
	private String langue;

	@TableField("task_name")
	@ApiModelProperty(name = "taskName", value = "任务名称", dataType = "String")
	private String taskName;

	@TableField("task_desc")
	@ApiModelProperty(name = "taskDesc", value = "任务说明", dataType = "String")
	private String taskDesc;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
