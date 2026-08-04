package com.playlet.internal.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 极光推送设备绑定（支持未登录）
 */
@Data
@TableName("app_push_device")
@ApiModel(value = "推送设备", description = "极光推送设备绑定")
public class AppPushDeviceEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("registration_id")
	@ApiModelProperty(name = "registrationId", value = "极光 registrationId", dataType = "String")
	private String registrationId;

	@TableField("device_name")
	@ApiModelProperty(name = "deviceName", value = "设备名称", dataType = "String")
	private String deviceName;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "绑定用户，未登录为空", dataType = "Integer")
	private Integer uid;

	@TableField("push_enabled")
	@ApiModelProperty(name = "pushEnabled", value = "推送开关：1开启 0关闭，默认1", dataType = "Integer")
	private Integer pushEnabled;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
