package com.playlet.internal.entity.version;

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
@TableName("app_version_config")
@ApiModel(value = "应用版本配置", description = "应用版本更新配置（文案见 app_version_i18n）")
public class AppVersionConfigEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("platform")
	@ApiModelProperty(name = "platform", value = "平台：android / ios / web / windows / mac", dataType = "String", example = "android")
	private String platform;

	@TableField("channel")
	@ApiModelProperty(name = "channel", value = "渠道：default / googleplay / appstore 等", dataType = "String", example = "default")
	private String channel;

	@TableField("version_code")
	@ApiModelProperty(name = "versionCode", value = "版本号整数，1.3.2 => 10302", dataType = "Integer", example = "10302")
	private Integer versionCode;

	@TableField("version_name")
	@ApiModelProperty(name = "versionName", value = "版本名", dataType = "String", example = "1.3.2")
	private String versionName;

	@TableField("is_force")
	@ApiModelProperty(name = "isForce", value = "是否强制更新：1是 0否", dataType = "Integer", example = "0")
	private Integer isForce;

	@TableField("download_url")
	@ApiModelProperty(name = "downloadUrl", value = "下载/商店地址", dataType = "String")
	private String downloadUrl;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "状态：0停用 1启用", dataType = "Integer", example = "1")
	private Integer status;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "运营备注", dataType = "String")
	private String remark;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;

	@TableField(exist = false)
	@ApiModelProperty(name = "langue", value = "列表/筛选语言码", dataType = "String")
	private String langue;

	@TableField(exist = false)
	@ApiModelProperty(name = "title", value = "当前语言更新标题", dataType = "String")
	private String title;

	@TableField(exist = false)
	@ApiModelProperty(name = "i18nList", value = "多语言文案", dataType = "List")
	private List<AppVersionI18nEntity> i18nList;
}
