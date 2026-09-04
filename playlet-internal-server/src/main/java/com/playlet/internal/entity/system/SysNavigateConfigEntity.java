package com.playlet.internal.entity.system;

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

@Data
@TableName("sys_navigate_config")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "导航开启隐藏配置",description = "导航开启隐藏配置")
public class SysNavigateConfigEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id",value = "主键",required = false,dataType = "Integer")
    private Integer id;
	
	@TableField("app_version")
	@ApiModelProperty(name = "appVersion",value = "app版本号",required = false,dataType = "appVersion")
    private String appVersion;
	
	@TableField("device_type")
	@ApiModelProperty(name = "deviceType",value = "设备类型",required = false,dataType = "deviceType")
    private String deviceType;
	
	@TableField("wallet_state")
	@ApiModelProperty(name = "walletState",value = "钱包 1开启2关闭",required = false,dataType = "String")
	private boolean walletState;
	
	@TableField("welfare_state")
	@ApiModelProperty(name = "welfareState",value = "福利1开启2关闭",required = false,dataType = "String")
	private boolean welfareState;

	@TableField("payPassword_state")
	@ApiModelProperty(name = "payPasswordState",value = "支付密码1开启2关闭",required = false,dataType = "String")
	private boolean payPasswordState;

	@TableField("site_state")
	@ApiModelProperty(name = "siteState",value = "设置1开启2关闭",required = false,dataType = "String")
	private boolean siteState;
	
	@TableField("setTime")
	@ApiModelProperty(name = "setTime",value = "创建时间",required = false,dataType = "Date")
    private Date setTime;
	
	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified",value = "更新时间",required = false,dataType = "Date")
	private Date gmtModified;
}
