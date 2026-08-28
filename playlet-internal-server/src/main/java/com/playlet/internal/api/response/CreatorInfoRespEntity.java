package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 作家资料返回。
 */
@Data
@ApiModel(value = "作家资料", description = "查询/修改资料")
public class CreatorInfoRespEntity {

	@ApiModelProperty("作家ID")
	private Integer id;

	@ApiModelProperty("登录邮箱")
	private String userAccount;

	@ApiModelProperty("展示昵称")
	private String nickname;

	@ApiModelProperty("头像（已签名）")
	private String avatar;

	@ApiModelProperty("手机区号")
	private String mobilePrefix;

	@ApiModelProperty("手机号")
	private String mobileNumber;

	@ApiModelProperty("账号状态 0注销 1正常 2冻结")
	private Integer userState;

	@ApiModelProperty("金币总余额")
	private Long coinBalance;

	@ApiModelProperty("冻结金币")
	private Long frozenCoinBalance;

	@ApiModelProperty("可用余额 = 总余额 - 冻结")
	private Long availableCoin;

	@ApiModelProperty("累计收益金币")
	private Long totalIncomeCoin;

	@ApiModelProperty("身份 1个人 2机构")
	private Integer identityType;

	@ApiModelProperty("账单地址")
	private String billAddress;

	@ApiModelProperty("入驻审核 0待审 1审核中 2通过 3驳回")
	private Integer auditStatus;

	@ApiModelProperty("入驻驳回原因")
	private String auditRejectReason;

	@ApiModelProperty("机构名称")
	private String orgName;

	@ApiModelProperty("证件正面（已签名）")
	private String idCardFront;

	@ApiModelProperty("证件背面（已签名）")
	private String idCardBack;

	@ApiModelProperty("最近登录")
	private Date lastLoginTime;

	@ApiModelProperty("注册时间")
	private Date setTime;

	@ApiModelProperty("钱包概要（余额/KYC/开卡/支付密码是否已设）；未开通为 null")
	private WalletUserInfoResp walletInfo;
}
