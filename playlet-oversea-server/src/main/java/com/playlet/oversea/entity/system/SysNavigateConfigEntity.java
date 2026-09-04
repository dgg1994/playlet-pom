package com.playlet.oversea.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import com.playlet.oversea.utils.jackson.OneAsTrueJsonDeserializer;
import com.playlet.oversea.utils.jackson.OneAsTrueJsonSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@TableName("sys_navigate_config")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "导航开启隐藏配置", description = "导航开启隐藏配置；库内 1/2，接口布尔仅 1 为 true")
public class SysNavigateConfigEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", required = false, dataType = "Integer")
	private Integer id;

	@TableField("app_version")
	@ApiModelProperty(name = "appVersion", value = "app版本号", required = false, dataType = "String")
	private String appVersion;

	@TableField("device_type")
	@ApiModelProperty(name = "deviceType", value = "设备类型", required = false, dataType = "String")
	private String deviceType;

	@TableField("wallet_state")
	@JsonSerialize(using = OneAsTrueJsonSerializer.class)
	@JsonDeserialize(using = OneAsTrueJsonDeserializer.class)
	@ApiModelProperty(name = "walletState", value = "钱包：库值1→true，其它→false", required = false, dataType = "Boolean")
	private Integer walletState;

	@TableField("welfare_state")
	@JsonSerialize(using = OneAsTrueJsonSerializer.class)
	@JsonDeserialize(using = OneAsTrueJsonDeserializer.class)
	@ApiModelProperty(name = "welfareState", value = "福利：库值1→true，其它→false", required = false, dataType = "Boolean")
	private Integer welfareState;

	@TableField("payPassword_state")
	@JsonSerialize(using = OneAsTrueJsonSerializer.class)
	@JsonDeserialize(using = OneAsTrueJsonDeserializer.class)
	@ApiModelProperty(name = "payPasswordState", value = "支付密码：库值1→true，其它→false", required = false, dataType = "Boolean")
	private Integer payPasswordState;

	@TableField("site_state")
	@JsonSerialize(using = OneAsTrueJsonSerializer.class)
	@JsonDeserialize(using = OneAsTrueJsonDeserializer.class)
	@ApiModelProperty(name = "siteState", value = "设置：库值1→true，其它→false", required = false, dataType = "Boolean")
	private Integer siteState;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", required = false, dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", required = false, dataType = "Date")
	private Date gmtModified;
}
