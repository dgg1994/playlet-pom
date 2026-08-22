package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 管理端创作者用户列表行。
 */
@Data
@ApiModel(value = "创作者用户管理列表项", description = "账号、资料、金币与入驻审核摘要")
public class CreatorAccountManageItemEntity {

	@ApiModelProperty("作家 id")
	private Integer id;

	@ApiModelProperty("登录邮箱")
	private String userAccount;

	@ApiModelProperty("展示昵称")
	private String nickname;

	@ApiModelProperty("手机号（含区号展示）")
	private String mobile;

	@ApiModelProperty("头像 URL（已签名）")
	private String avatarUrl;

	@ApiModelProperty("账号状态：0注销 1正常 2冻结")
	private Integer userState;

	@ApiModelProperty("账号状态文案")
	private String userStateLabel;

	@ApiModelProperty("1个人创作者 2创作机构")
	private Integer identityType;

	@ApiModelProperty("身份类型文案")
	private String identityTypeLabel;

	@ApiModelProperty("真实姓名/法人姓名")
	private String realName;

	@ApiModelProperty("入驻审核：0待审 1审核中 2通过 3驳回")
	private Integer auditStatus;

	@ApiModelProperty("入驻审核文案")
	private String auditStatusLabel;

	@ApiModelProperty("OnePay 绑定 0未绑定 1已绑定")
	private Integer onepayBindStatus;

	@ApiModelProperty("可用金币（总余额-冻结）")
	private Long availableCoin;

	@ApiModelProperty("金币总余额")
	private Long coinBalance;

	@ApiModelProperty("冻结金币")
	private Long frozenCoinBalance;

	@ApiModelProperty("累计收益金币")
	private Long totalIncomeCoin;

	@ApiModelProperty("最近登录时间")
	private Date lastLoginTime;

	@ApiModelProperty("注册时间")
	private Date setTime;
}
