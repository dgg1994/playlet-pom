package com.playlet.internal.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_account")
@ApiModel(value = "应用账户",description = "应用账户")
public class AppAccountEntity extends PageQueryHelperEntity {
	
	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id",value = "主键",required = false,dataType = "Integer")
    private Integer id;

	@TableField("uid")
	@ApiModelProperty(name = "uid",value = "三方id",required = false,dataType = "String")
	private Integer uid;
	
	@TableField("user_password")
	@ApiModelProperty(name = "userPassword",value = "登录密码",required = true,dataType = "String")
	private String userPassword;
	
	@TableField("pay_password")
	@ApiModelProperty(name = "payPassword",value = "支付密码",required = false,dataType = "String")
	private String payPassword;
	
	@TableField("user_account")
	@ApiModelProperty(name = "userAccount",value = "账号",required = true,dataType = "String")
	private String userAccount;
	
	@TableField("user_email")
	@ApiModelProperty(name = "userEmail",value = "邮箱",required = true,dataType = "String")
	private String userEmail;
	
	@TableField("mobile_number")
	@ApiModelProperty(name = "mobileNumber",value = "手机号",required = false,dataType = "String")
	private String mobileNumber;
	
	@TableField("mobile_prefix")
	@ApiModelProperty(name = "mobilePrefix",value = "国家代码",required = false,dataType = "String")
	private String mobilePrefix;
	
	@TableField("google_secretkey")
	@ApiModelProperty(name = "googleSecretkey",value = "谷歌密钥",required = false,dataType = "String")
    private String googleSecretkey;
	
	@TableField("user_state")
	@ApiModelProperty(name = "userState",value = "用户状态",required = false,dataType = "String")
	private Integer userState;
	
	@TableField("invitation_code")
	@ApiModelProperty(name = "invitationCode",value = "自身的邀请码",required = false,dataType = "String")
	private String invitationCode;

	@TableField("invited_by_uid")
	@ApiModelProperty(name = "invitedByUid",value = "邀请人uid",required = false,dataType = "String")
	private String invitedByUid;

	@TableField("register_source")
	@ApiModelProperty(name = "registerSource",value = "来源如果是一键注册=1 否则=2",required = false,dataType = "String")
	private Integer registerSource;

	@TableField("registration_id")
	@ApiModelProperty(name = "registrationId",value = "Jpush三方id",required = true,dataType = "String")
	private String registrationId;

	@TableField("nickname")
	@ApiModelProperty(name = "nickname",value = "昵称",required = false,dataType = "String")
	private String nickname;

	@TableField("avatar")
	@ApiModelProperty(name = "avatar",value = "头像URL",required = false,dataType = "String")
	private String avatar;

	@TableField("coin_balance")
	@ApiModelProperty(name = "coinBalance",value = "金币余额",required = false,dataType = "Long")
	private Long coinBalance;
	
	@TableField("setTime")
	@ApiModelProperty(name = "setTime",value = "注册时间",required = false,dataType = "Date")
    private Date setTime;
	
	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified",value = "更新时间",required = false,dataType = "Date")
	private Date gmtModified;
	
	@TableField(exist = false)
	@ApiModelProperty(name = "emailCode",value = "邮箱验证码",required = false,dataType = "Integer")
	private String emailCode;
	
	@TableField(exist = false)
	@ApiModelProperty(name = "telCode",value = "手机号验证码",required = false,dataType = "Integer")
	private String telCode;
	
	@TableField(exist = false)
	@ApiModelProperty(name = "deviceName",value = "设备名称",required = false,dataType = "Integer")
	private String deviceName;
	
	@TableField(exist = false)
	@ApiModelProperty(name = "cid",value = "极光推送cid",required = false,dataType = "Integer")
	private String cid;
	
	@TableField(exist = false)
	@ApiModelProperty(name = "enterInvitationCode",value = "填写的邀请码",required = false,dataType = "Integer")
	private String enterInvitationCode;
	
	@TableField(exist = false)
	@ApiModelProperty(name = "loginType",value = "登录方式（1邮箱2手机号）",required = false,dataType = "Integer")
	private Integer loginType;

	/** 一键登录渠道：与 LoginTypeEnums 一致，1=Apple，2=Google */
	@TableField(exist = false)
	@ApiModelProperty(name = "type", value = "一键登录：1 Apple，2 Google", required = false, dataType = "Integer")
	private Integer type;

	@TableField(exist = false)
	@ApiModelProperty(name = "idToken", value = "Apple/Google 返回的 id_token", required = false, dataType = "String")
	private String idToken;

	@TableField(exist = false)
	@ApiModelProperty(name = "followCount",value = "我的关注",required = false,dataType = "Long")
	private Long followCount;

	@TableField(exist = false)
	@ApiModelProperty(name = "fansCount",value = "我的粉丝",required = false,dataType = "Long")
	private Long fansCount;

	@TableField(exist = false)
	@ApiModelProperty(name = "likeCount",value = "获赞数",required = false,dataType = "Long")
	private Long likeCount;

}
