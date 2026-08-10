package com.playlet.oversea.entity.medal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("medal_config_i18n")
@ApiModel(value = "勋章多语言", description = "勋章名称/说明多语言")
public class MedalConfigI18nEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("medal_id")
	@ApiModelProperty(name = "medalId", value = "勋章id", dataType = "Integer")
	private Integer medalId;

	@TableField("langue")
	@ApiModelProperty(name = "langue", value = "语言码", dataType = "String")
	private String langue;

	@TableField("medal_name")
	@ApiModelProperty(name = "medalName", value = "勋章名称", dataType = "String")
	private String medalName;

	@TableField("slogan")
	@ApiModelProperty(name = "slogan", value = "副文案/Slogan", dataType = "String")
	private String slogan;

	@TableField("condition_text")
	@ApiModelProperty(name = "conditionText", value = "条件展示文案", dataType = "String")
	private String conditionText;

	@TableField("share_title")
	@ApiModelProperty(name = "shareTitle", value = "炫耀分享标题", dataType = "String")
	private String shareTitle;

	@TableField("share_desc")
	@ApiModelProperty(name = "shareDesc", value = "炫耀分享描述", dataType = "String")
	private String shareDesc;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
