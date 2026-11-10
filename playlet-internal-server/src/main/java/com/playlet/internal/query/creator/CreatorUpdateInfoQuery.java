package com.playlet.internal.query.creator;

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

	@ApiModelProperty(name = "creatorRole", value = "入驻身份 1出品方 2MCN机构 3独立创作者")
	private Integer creatorRole;

	@ApiModelProperty(name = "idCardFront", value = "证件正面图")
	private String idCardFront;

	@ApiModelProperty(name = "idCardBack", value = "证件背面图")
	private String idCardBack;

	@ApiModelProperty(name = "billAddress", value = "账单寄送地址")
	private String billAddress;

	@ApiModelProperty(name = "orgName", value = "机构名称")
	private String orgName;

	@ApiModelProperty(name = "orgLicense", value = "营业执照图")
	private String orgLicense;

	@ApiModelProperty(name = "city", value = "所在城市")
	private String city;

	@ApiModelProperty(name = "expertise", value = "擅长领域，顿号分隔")
	private String expertise;

	@ApiModelProperty(name = "intro", value = "个人/团队简介")
	private String intro;
}
