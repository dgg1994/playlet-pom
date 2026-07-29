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

/**
 * 角色表 sys_role
 * 
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_info")
@ApiModel(value = "系统信息", description = "系统信息")
public class SysInfoEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "id", required = true, dataType = "Integer")
	private Integer id;
	
	@TableField("config_type")
	@ApiModelProperty(name = "configType", value = "配置类型", required = true, dataType = "String")
	private Integer configType;
	
	@TableField("config_type_name")
	@ApiModelProperty(name = "configTypeName", value = "配置类型", required = true, dataType = "String")
	private String configTypeName;
	
	@TableField("config_lable")
	@ApiModelProperty(name = "configLable", value = "配置标签", required = true, dataType = "String")
	private Integer configLable;

    @TableField("language")
    @ApiModelProperty(name = "language", value = "配置语言", required = true, dataType = "String")
    private String language;

	@TableField("config_name")
	@ApiModelProperty(name = "configName", value = "配置名称", required = true, dataType = "String")
	private String configName;

	@TableField("config_content")
	@ApiModelProperty(name = "configContent", value = "配置内容", required = true, dataType = "String")
	private String configContent;
	
	@TableField("config_url")
	@ApiModelProperty(name = "configUrl", value = "html地址", required = true, dataType = "String")
	private String configUrl;

	@TableField("status")
	@ApiModelProperty(name = "status", value = " 配置状态（1正常 2停用）", required = true, dataType = "Integer")
	private Integer status;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime",value = "注册时间",required = false,dataType = "Date")
    private Date setTime;
	
	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified",value = "更新时间",required = false,dataType = "Date")
	private Date gmtModified;
	
	@TableField(exist = false)
    @ApiModelProperty(name = "languageName", value = "配置语言", required = true, dataType = "String")
    private String languageName;


}
