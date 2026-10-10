package com.playlet.internal.entity.prefer;

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

/**
 * 看剧喜好-分类字典（频道/时代背景/风格/主题情节/角色）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dic_watch_prefer_category")
@ApiModel(value = "看剧喜好分类", description = "看剧喜好页区块分类字典")
public class WatchPreferCategoryEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("category_code")
	@ApiModelProperty(name = "categoryCode", value = "分类编码：CHANNEL/ERA/STYLE/THEME/ROLE", dataType = "String")
	private String categoryCode;

	@TableField("langue")
	@ApiModelProperty(name = "langue", value = "语言，如 zh-cn", dataType = "String")
	private String langue;

	@TableField("category_name")
	@ApiModelProperty(name = "categoryName", value = "分类展示名", dataType = "String")
	private String categoryName;

	@TableField("hint_text")
	@ApiModelProperty(name = "hintText", value = "分类提示文案，如至少选择1个", dataType = "String")
	private String hintText;

	@TableField("min_select")
	@ApiModelProperty(name = "minSelect", value = "最少必选个数", dataType = "Integer")
	private Integer minSelect;

	@TableField("max_select")
	@ApiModelProperty(name = "maxSelect", value = "最多可选个数；0不限制", dataType = "Integer")
	private Integer maxSelect;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "区块排序，越大越靠前", dataType = "Integer")
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
