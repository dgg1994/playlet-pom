package com.playlet.internal.entity.drama;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("drama_tag_rel")
@ApiModel(value = "短剧标签关联", description = "短剧标签关联")
public class DramaTagRelEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", required = false, dataType = "Integer")
	private Integer id;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId", value = "业务剧ID", required = true, dataType = "String")
	private String dramaId;

	@TableField("tag_id")
	@ApiModelProperty(name = "tagId", value = "业务标签ID", required = true, dataType = "String")
	private String tagId;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "该剧下标签排序", required = false, dataType = "Integer")
	private Integer sortWeight;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", required = false, dataType = "Date")
	private Date setTime;

	@TableField(exist = false)
	@ApiModelProperty(name = "drama", value = "短剧信息", required = false, dataType = "DramaEntity")
	private DramaEntity drama;
}
