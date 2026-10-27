package com.playlet.internal.query.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "修改密码入参", description = "修改密码入参")
public class UpdatePwdEntity {

	@NotBlank(message = "用户uid不能为空")
	@ApiModelProperty(name = "uid",value = "用户uid",required = true,dataType = "String")
    private Integer uid;
	
	@ApiModelProperty(name = "formerPassword",value = "原密码",required = false,dataType = "String")
    private String formerPassword;
	
	@ApiModelProperty(name = "newPassword",value = "新密码",required = true,dataType = "String")
    private String newPassword;

	@ApiModelProperty(name = "emailCode",value = "邮箱验证码",required = true,dataType = "String")
	private String emailCode;
	
	@ApiModelProperty(name = "email",value = "邮箱（邮箱找回时必填）",required = false,dataType = "String")
	private String email;

	@ApiModelProperty(name = "mobileNumber",value = "手机号（不含区号；手机号找回时必填）",required = false,dataType = "String")
	private String mobileNumber;

	@ApiModelProperty(name = "mobilePrefix",value = "国家区号，如 86 / +86（手机号找回时必填）",required = false,dataType = "String")
	private String mobilePrefix;

	@ApiModelProperty(name = "telCode",value = "短信验证码（手机号找回时必填）",required = false,dataType = "String")
	private String telCode;

}
