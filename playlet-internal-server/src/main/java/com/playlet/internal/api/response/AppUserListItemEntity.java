package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端用户列表行。
 */
@Data
@ApiModel(value = "用户列表项", description = "用户管理表格行")
public class AppUserListItemEntity {

	@ApiModelProperty("用户ID")
	private Integer uid;

	@ApiModelProperty("昵称")
	private String nickname;

	@ApiModelProperty("头像签名 URL")
	private String avatarUrl;

	@ApiModelProperty("脱敏手机号")
	private String mobileMasked;

	@ApiModelProperty("实名姓名")
	private String realName;

	@ApiModelProperty("注册来源码")
	private Integer registerSource;

	@ApiModelProperty("邀请人 uid")
	private String invitedByUid;

	@ApiModelProperty("来源渠道文案")
	private String sourceChannelLabel;

	@ApiModelProperty("会员状态码 0普通 1有效 2已过期")
	private Integer memberStatus;

	@ApiModelProperty("会员状态文案")
	private String memberStatusLabel;

	@ApiModelProperty("金币余额")
	private Long coinBalance;

	@ApiModelProperty("累计充值(元)")
	private BigDecimal totalRechargeYuan;

	@ApiModelProperty("账号状态")
	private Integer userState;

	@ApiModelProperty("账号状态文案")
	private String userStateLabel;

	@ApiModelProperty("是否禁言中")
	private Boolean muted;

	@ApiModelProperty("禁言截止（内部映射用）")
	private Date muteUntil;

	@ApiModelProperty("注册时间")
	private Date setTime;

	@ApiModelProperty("最后登录")
	private Date lastLoginTime;
}
