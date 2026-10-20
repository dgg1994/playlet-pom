package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端用户详情。
 */
@Data
@ApiModel(value = "用户详情", description = "基本资料 + 会员 + 资产 + 概览")
public class AppUserDetailRespEntity {

	@ApiModelProperty("用户ID")
	private Integer uid;

	@ApiModelProperty("昵称")
	private String nickname;

	@ApiModelProperty("实名姓名")
	private String realName;

	@ApiModelProperty("头像签名 URL")
	private String avatarUrl;

	@ApiModelProperty("脱敏手机号")
	private String mobileMasked;

	@ApiModelProperty("邮箱（脱敏）")
	private String emailMasked;

	@ApiModelProperty("来源渠道文案")
	private String sourceChannelLabel;

	@ApiModelProperty("注册设备")
	private String deviceName;

	@ApiModelProperty("邀请人 uid；无则空")
	private String invitedByUid;

	@ApiModelProperty("账号状态")
	private Integer userState;

	@ApiModelProperty("账号状态文案")
	private String userStateLabel;

	@ApiModelProperty("是否禁言中")
	private Boolean muted;

	@ApiModelProperty("禁言截止")
	private Date muteUntil;

	@ApiModelProperty("注册时间")
	private Date setTime;

	@ApiModelProperty("最后登录")
	private Date lastLoginTime;

	@ApiModelProperty("注册天数")
	private Integer registerDays;

	@ApiModelProperty("会员等级文案")
	private String memberLevelLabel;

	@ApiModelProperty("会员状态码 0普通 1有效 2已过期")
	private Integer memberStatus;

	@ApiModelProperty("会员状态文案")
	private String memberStatusLabel;

	@ApiModelProperty("会员开通时间")
	private Date memberActiveTime;

	@ApiModelProperty("会员到期时间")
	private Date memberExpireTime;

	@ApiModelProperty("累计充值(元)")
	private BigDecimal totalRechargeYuan;

	@ApiModelProperty("累计消费(元)：会员现金+单集现金")
	private BigDecimal totalConsumeYuan;

	@ApiModelProperty("金币余额")
	private Long coinBalance;

	@ApiModelProperty("冻结金币")
	private Long frozenCoinBalance;

	@ApiModelProperty("可用金币")
	private Long availableCoin;

	@ApiModelProperty("看过短剧部数")
	private Integer watchedDramaCount;

	@ApiModelProperty("累计观看小时（取整）")
	private Long watchHours;

	@ApiModelProperty("自动续费：当前未实现，固定 false")
	private Boolean autoRenew;
}
