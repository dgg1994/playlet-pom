package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理端编辑用户资料。
 */
@Data
@ApiModel(value = "编辑用户资料", description = "管理端修改昵称/实名/头像/手机号等")
public class AppUserProfileUpdateQuery {

	@ApiModelProperty(value = "用户ID", required = true)
	private Integer uid;

	@ApiModelProperty("昵称")
	private String nickname;

	@ApiModelProperty("实名姓名")
	private String realName;

	@ApiModelProperty("头像 key/URL")
	private String avatar;

	@ApiModelProperty("手机号")
	private String mobileNumber;

	@ApiModelProperty("国家区号")
	private String mobilePrefix;

	@ApiModelProperty("性别 0未知 1男 2女")
	private Integer gender;

	@ApiModelProperty("出生年月 yyyy-MM")
	private String birthMonth;
}
