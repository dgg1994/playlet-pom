package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * APP 用户信息（管理端/列表展示）。
 */
@Data
@ApiModel(value = "APP用户信息", description = "APP用户信息")
public class AppUserInfoReqEntity {

	@ApiModelProperty(name = "id", value = "主键", required = false, dataType = "Integer")
	private Integer id;

	@ApiModelProperty(name = "uid", value = "用户 id", required = false, dataType = "String")
	private String uid;

	@ApiModelProperty(name = "userPassword", value = "登录密码", required = true, dataType = "String")
	private String userPassword;

	@ApiModelProperty(name = "userAccount", value = "账号", required = true, dataType = "String")
	private String userAccount;

	@ApiModelProperty(name = "userEmail", value = "邮箱", required = true, dataType = "String")
	private String userEmail;

	@ApiModelProperty(name = "mobileNumber", value = "手机号", required = false, dataType = "String")
	private String mobileNumber;

	@ApiModelProperty(name = "mobilePrefix", value = "手机区号", required = false, dataType = "String")
	private String mobilePrefix;

	@ApiModelProperty(name = "googleSecretkey", value = "谷歌密钥", required = false, dataType = "String")
	private String googleSecretkey;

	@ApiModelProperty(name = "userState", value = "用户状态", required = false, dataType = "String")
	private Integer userState;

	@ApiModelProperty(name = "invitationCode", value = "自身的邀请码", required = false, dataType = "String")
	private String invitationCode;

	@ApiModelProperty(name = "registrationId", value = "Jpush三方id", required = false, dataType = "String")
	private String registrationId;

	@ApiModelProperty(name = "setTime", value = "注册时间", required = false, dataType = "Date")
	private Date setTime;

	@ApiModelProperty(name = "gmtModified", value = "更新时间", required = false, dataType = "Date")
	private Date gmtModified;
}
