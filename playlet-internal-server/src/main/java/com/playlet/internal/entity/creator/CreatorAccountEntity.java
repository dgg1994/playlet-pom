package com.playlet.internal.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 作家登录账号。
 */
@Data
@TableName("creator_account")
@ApiModel(value = "作家登录账号", description = "作家端登录主体，user_account 即邮箱")
public class CreatorAccountEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键，对应 drama.belong_user")
	private Integer id;

	@TableField("user_account")
	@ApiModelProperty(name = "userAccount", value = "登录账号（邮箱）")
	private String userAccount;

	@TableField("user_password")
	@ApiModelProperty(name = "userPassword", value = "登录密码")
	@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	@com.alibaba.fastjson.annotation.JSONField(serialize = false)
	private String userPassword;

	@TableField("mobile_prefix")
	@ApiModelProperty(name = "mobilePrefix", value = "手机区号")
	private String mobilePrefix;

	@TableField("mobile_number")
	@ApiModelProperty(name = "mobileNumber", value = "手机号")
	private String mobileNumber;

	@TableField("nickname")
	@ApiModelProperty(name = "nickname", value = "展示昵称")
	private String nickname;

	@TableField("avatar")
	@ApiModelProperty(name = "avatar", value = "头像 key/URL")
	private String avatar;

	@TableField("user_state")
	@ApiModelProperty(name = "userState", value = "0注销 1正常 2冻结")
	private Integer userState;

	@TableField("coin_balance")
	@ApiModelProperty(name = "coinBalance", value = "金币总余额（含冻结）")
	private Long coinBalance;

	@TableField("frozen_coin_balance")
	@ApiModelProperty(name = "frozenCoinBalance", value = "冻结金币")
	private Long frozenCoinBalance;

	@TableField("total_income_coin")
	@ApiModelProperty(name = "totalIncomeCoin", value = "累计收益金币")
	private Long totalIncomeCoin;

	@TableField("last_login_time")
	@ApiModelProperty(name = "lastLoginTime", value = "最近登录时间")
	private Date lastLoginTime;

	@TableField("sys_msg_read_publish_id")
	@ApiModelProperty(name = "sysMsgReadPublishId", value = "站务广播已读游标")
	private Long sysMsgReadPublishId;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "注册时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
