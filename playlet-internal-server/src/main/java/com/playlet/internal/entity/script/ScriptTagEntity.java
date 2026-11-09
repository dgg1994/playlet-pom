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
 * 剧本市场-标签字典。
 */
@Data
@TableName("dic_script_tag")
@ApiModel(value = "剧本标签", description = "题材/元素标签字典")
public class ScriptTagEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("group_id")
	@ApiModelProperty(name = "groupId", value = "分组：topic题材 element元素")
	private String groupId;

	@TableField("langue")
	@ApiModelProperty(name = "langue", value = "语言")
	private String langue;

	@TableField("tag_name")
	@ApiModelProperty(name = "tagName", value = "标签名称")
	private String tagName;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "排序权重")
	private Integer sortWeight;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1启用 0停用")
	private Integer status;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
