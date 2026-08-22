package com.playlet.oversea.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 作家修改资料请求。
 */
@Data
@ApiModel(value = "作家修改资料", description = "不可改登录邮箱")
public class CreatorUpdateInfoQuery {

	@ApiModelProperty(name = "nickname", value = "展示昵称，最多9字")
	private String nickname;

	@ApiModelProperty(name = "avatar", value = "头像")
	private String avatar;

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

	@ApiModelProperty(name = "onepayAccount", value = "已废弃：请调用 bindOnePay / unBindOnePay")
	private String onepayAccount;

	@ApiModelProperty(name = "billAddress", value = "账单寄送地址")
	private String billAddress;

	@ApiModelProperty(name = "orgName", value = "机构名称")
	private String orgName;

	@ApiModelProperty(name = "orgLicense", value = "营业执照图")
	private String orgLicense;
}
