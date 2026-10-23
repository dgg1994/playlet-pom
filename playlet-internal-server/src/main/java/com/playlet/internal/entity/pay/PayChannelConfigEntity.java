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
 * 支付渠道配置（微信/支付宝/抖音）。
 */
@Data
@TableName("pay_channel_config")
@ApiModel(value = "支付渠道配置", description = "支付渠道商户参数与启用状态")
public class PayChannelConfigEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("channel")
	@ApiModelProperty(name = "channel", value = "支付渠道 WECHAT/ALIPAY/DOUYIN", dataType = "String")
	private String channel;

	@TableField("channel_name")
	@ApiModelProperty(name = "channelName", value = "渠道名称", dataType = "String")
	private String channelName;

	@TableField("merchant_no")
	@ApiModelProperty(name = "merchantNo", value = "商户号", dataType = "String")
	private String merchantNo;

	@TableField("app_id")
	@ApiModelProperty(name = "appId", value = "应用ID/APPID", dataType = "String")
	private String appId;

	@TableField("enabled")
	@ApiModelProperty(name = "enabled", value = "1启用 0停用", dataType = "Integer")
	private Integer enabled;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "备注", dataType = "String")
	private String remark;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
