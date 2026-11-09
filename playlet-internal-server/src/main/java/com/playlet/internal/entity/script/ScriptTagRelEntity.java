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
 * 剧本市场-剧本标签关联。
 */
@Data
@TableName("script_tag_rel")
@ApiModel(value = "剧本标签关联", description = "剧本与标签多对多")
public class ScriptTagRelEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("script_id")
	@ApiModelProperty(name = "scriptId", value = "script.id")
	private Integer scriptId;

	@TableField("tag_id")
	@ApiModelProperty(name = "tagId", value = "dic_script_tag.id")
	private Integer tagId;

	@TableField("sort_order")
	@ApiModelProperty(name = "sortOrder", value = "展示排序")
	private Integer sortOrder;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;
}
