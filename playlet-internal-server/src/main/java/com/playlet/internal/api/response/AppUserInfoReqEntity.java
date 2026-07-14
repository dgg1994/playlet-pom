package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class AppUserInfoReqEntity {
	
	@ApiModelProperty(name = "id",value = "主键",required = false,dataType = "Integer")
    private Integer id;
	
	@ApiModelProperty(name = "uid",value = "三方id",required = false,dataType = "String")
	private String uid;
	
	@ApiModelProperty(name = "userPassword",value = "登录密码",required = true,dataType = "String")
	private String userPassword;
	
	@ApiModelProperty(name = "payPassword",value = "支付密码",required = false,dataType = "String")
	private String payPassword;
	
	@ApiModelProperty(name = "userAccount",value = "账号",required = true,dataType = "String")
	private String userAccount;
	
	@ApiModelProperty(name = "userEmail",value = "邮箱",required = true,dataType = "String")
	private String userEmail;
	
	@ApiModelProperty(name = "mobileNumber",value = "手机号",required = false,dataType = "String")
	private String mobileNumber;
	
	@ApiModelProperty(name = "mobilePrefix",value = "国家代码",required = false,dataType = "String")
	private String mobilePrefix;
	
	@ApiModelProperty(name = "googleSecretkey",value = "谷歌密钥",required = false,dataType = "String")
    private String googleSecretkey;
	
	@ApiModelProperty(name = "userState",value = "用户状态",required = false,dataType = "String")
	private Integer userState;
	
	@ApiModelProperty(name = "invitationCode",value = "自身的邀请码",required = false,dataType = "String")
	private String invitationCode;

	@ApiModelProperty(name = "registrationId",value = "Jpush三方id",required = false,dataType = "String")
	private String registrationId;
	
	@ApiModelProperty(name = "setTime",value = "注册时间",required = false,dataType = "Date")
    private Date setTime;
	
	@ApiModelProperty(name = "gmtModified",value = "更新时间",required = false,dataType = "Date")
	private Date gmtModified;
	
	@ApiModelProperty(name = "walletState",value = "钱包开启状态；ture开启；false关闭",required = false,dataType = "String")
	private Boolean walletState;
	
	@ApiModelProperty(name = "walletBalance",value = "钱包余额",required = false,dataType = "Double")
	private Double walletBalance;
	
	@ApiModelProperty(name = "freezeBalance",value = "冻结资产",required = false,dataType = "Double")
	private Double freezeBalance;
	
	@ApiModelProperty(name = "openFreezeBalance",value = "开卡临时冻结",required = false,dataType = "Double")
	private Double openFreezeBalance;
	
	@ApiModelProperty(name = "topupTotalBalance",value = "累计充值金额",required = false,dataType = "Double")
	private Double topupTotalBalance;
	
	@ApiModelProperty(name = "kycState",value = "kyc认证状态",required = false,dataType = "boolean")
	private Integer kycState;
	
	@ApiModelProperty(name = "kycStateName",value = "kyc认证状态",required = false,dataType = "boolean")
	private String kycStateName;
	
	@ApiModelProperty(name = "kycAuditResult",value = "kyc申请结果描述",required = false,dataType = "String")
    private String kycAuditResult;
	
	@ApiModelProperty(name = "activationState",value = "是否开卡激活",required = false,dataType = "Integer")
    private Integer activationState;
	
	@ApiModelProperty(name = "activationTime",value = "激活时间",required = false,dataType = "Date")
    private Date activationTime;
	
	@ApiModelProperty(name = "freeLable",value = "是否免除开卡费用（1否，2是）",required = false,dataType = "Integer")
    private Integer freeLable;

}
