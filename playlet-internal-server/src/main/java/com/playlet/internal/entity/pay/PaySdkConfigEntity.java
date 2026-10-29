package com.playlet.internal.entity.pay;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 支付 SDK 商户参数配置（application.yml pay.* 的数据库覆盖配置）。
 */
@Data
@TableName("pay_sdk_config")
@ApiModel(value = "支付SDK参数配置", description = "DB 有值时覆盖 application.yml pay.*，空值回落 yml 默认")
public class PaySdkConfigEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("config_key")
	@ApiModelProperty(name = "configKey", value = "配置键，如 pay.wechat.app-id", dataType = "String")
	private String configKey;

	@TableField("config_value")
	@ApiModelProperty(name = "configValue", value = "配置值；encrypted=1 时为 AES 加密后 Base64", dataType = "String")
	private String configValue;

	@TableField("encrypted")
	@ApiModelProperty(name = "encrypted", value = "是否加密存储 1是 0否", dataType = "Integer")
	private Integer encrypted;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "说明", dataType = "String")
	private String remark;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
