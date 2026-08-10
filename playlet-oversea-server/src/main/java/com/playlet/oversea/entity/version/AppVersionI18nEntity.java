package com.playlet.oversea.entity.version;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("app_version_i18n")
@ApiModel(value = "应用版本多语言", description = "更新标题/说明多语言")
public class AppVersionI18nEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("version_id")
	@ApiModelProperty(name = "versionId", value = "版本配置ID", dataType = "Integer")
	private Integer versionId;

	@TableField("langue")
	@ApiModelProperty(name = "langue", value = "语言码：zh-cn / en / ja-jp / pt-br 等", dataType = "String", example = "zh-cn")
	private String langue;

	@TableField("title")
	@ApiModelProperty(name = "title", value = "更新标题", dataType = "String", example = "发现新版本 1.3.2")
	private String title;

	@TableField("content")
	@ApiModelProperty(name = "content", value = "更新说明，支持换行", dataType = "String")
	private String content;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
