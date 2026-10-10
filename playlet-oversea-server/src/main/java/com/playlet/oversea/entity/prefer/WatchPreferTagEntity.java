package com.playlet.oversea.entity.prefer;

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

/**
 * 看剧喜好-选项字典（分类下的可选标签）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dic_watch_prefer_tag")
@ApiModel(value = "看剧喜好选项", description = "看剧喜好分类下的选项字典")
public class WatchPreferTagEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("group_id")
	@ApiModelProperty(name = "groupId", value = "选项语义分组ID（跨语言同一选项共用）", dataType = "String")
	private String groupId;

	@TableField("category_code")
	@ApiModelProperty(name = "categoryCode", value = "所属分类编码", dataType = "String")
	private String categoryCode;

	@TableField("langue")
	@ApiModelProperty(name = "langue", value = "语言，如 zh-cn", dataType = "String")
	private String langue;

	@TableField("tag_name")
	@ApiModelProperty(name = "tagName", value = "选项展示名", dataType = "String")
	private String tagName;

	@TableField("icon_url")
	@ApiModelProperty(name = "iconUrl", value = "选项图标URL", dataType = "String")
	private String iconUrl;

	@TableField("drama_tag_group_id")
	@ApiModelProperty(name = "dramaTagGroupId", value = "映射 dic_drama_tag.group_id，二期推荐用", dataType = "String")
	private String dramaTagGroupId;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "同分类内排序，越大越靠前", dataType = "Integer")
	private Integer sortWeight;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1启用 0停用", dataType = "Integer")
	private Integer status;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
