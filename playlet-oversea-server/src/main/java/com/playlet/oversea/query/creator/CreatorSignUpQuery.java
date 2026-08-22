package com.playlet.oversea.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 作家注册请求（用户名即邮箱）。
 */
@Data
@ApiModel(value = "作家注册", description = "邮箱验证码注册，userAccount 即登录邮箱")
public class CreatorSignUpQuery {

	@NotBlank
	@ApiModelProperty(name = "userAccount", value = "登录邮箱", required = true)
	private String userAccount;

	@NotBlank
	@ApiModelProperty(name = "userPassword", value = "登录密码", required = true)
	private String userPassword;

	@ApiModelProperty(name = "confirmPassword", value = "确认密码")
	private String confirmPassword;

	@NotBlank
	@ApiModelProperty(name = "emailCode", value = "邮箱验证码", required = true)
	private String emailCode;

	@ApiModelProperty(name = "mobilePrefix", value = "手机区号")
	private String mobilePrefix;

	@ApiModelProperty(name = "mobileNumber", value = "手机号")
	private String mobileNumber;

	@ApiModelProperty(name = "identityType", value = "1个人创作者 2创作机构")
	private Integer identityType;

	@ApiModelProperty(name = "idCardFront", value = "证件正面图")
	private String idCardFront;

	@ApiModelProperty(name = "idCardBack", value = "证件背面图")
	private String idCardBack;

	@ApiModelProperty(name = "onepayAccount", value = "已废弃：请调用 bindOnePay")
	private String onepayAccount;

	@ApiModelProperty(name = "billAddress", value = "账单寄送地址")
	private String billAddress;

	@ApiModelProperty(name = "orgName", value = "机构名称")
	private String orgName;

	@ApiModelProperty(name = "orgLicense", value = "营业执照图")
	private String orgLicense;
}
