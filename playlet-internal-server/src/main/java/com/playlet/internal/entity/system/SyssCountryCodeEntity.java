package com.playlet.internal.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("sys_country_code")
public class SyssCountryCodeEntity {
	
	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "id", required = true, dataType = "Integer")
	private Long id;
	
	@TableField("name")
	@ApiModelProperty(name = "name", value = "国家名称", required = false, dataType = "String")
	private String name;
	
	@TableField("dial_code")
	@ApiModelProperty(name = "dial_code", value = "国家代码", required = false, dataType = "String")
	private String dialCode;
	
	@TableField("code")
	@ApiModelProperty(name = "code", value = "国家编号", required = false, dataType = "String")
	private String code;
	
	@TableField("language")
	@ApiModelProperty(name = "language", value = "语言", required = false, dataType = "String")
	private String language;

}
