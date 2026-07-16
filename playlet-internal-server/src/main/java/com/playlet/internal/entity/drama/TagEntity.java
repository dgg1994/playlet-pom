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
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dic_drama_tag")
@ApiModel(value = "短剧标签", description = "短剧标签")
public class TagEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", required = false, dataType = "Integer")
	private Integer id;

	@TableField("group_id")
	@ApiModelProperty(name = "groupId", value = "分组id", required = true, dataType = "String")
	private String groupId;

	@TableField("langue")
	@ApiModelProperty(name = "langue", value = "语言", required = true, dataType = "String")
	private String langue;

	@TableField("tag_name")
	@ApiModelProperty(name = "tagName", value = "标签名称", required = true, dataType = "String")
	private String tagName;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "排序权重，越大越靠前", required = false, dataType = "Integer")
	private Integer sortWeight;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1启用0停用", required = false, dataType = "Integer")
	private Integer status;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", required = false, dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", required = false, dataType = "Date")
	private Date gmtModified;

	@TableField(exist = false)
	@ApiModelProperty(name = "dramaId", value = "业务剧ID（绑定用）", required = false, dataType = "String")
	private String dramaId;

	@TableField(exist = false)
	@ApiModelProperty(name = "tagIds", value = "标签ID列表（批量绑定）", required = false, dataType = "List")
	private List<String> tagIds;
}
