package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 钱包用户信息（不含密码）。
 */
@Data
@ApiModel(value = "钱包用户信息", description = "余额/KYC/开卡/是否已设支付密码")
public class WalletUserInfoResp {

	@ApiModelProperty("本地 wallet_user.id")
	private Long walletUserId;

	@ApiModelProperty("钱包三方 uid")
	private Long walletUid;

	@ApiModelProperty("主体 1=C端 2=作家")
	private Integer userType;

	@ApiModelProperty("邮箱")
	private String email;

	@ApiModelProperty("账户可用余额缓存（USD 等）")
	private BigDecimal availableBalance;

	@ApiModelProperty("冻结余额缓存")
	private BigDecimal freezeBalance;

	@ApiModelProperty("开卡临时冻结缓存")
	private BigDecimal openFreezeBalance;

	@ApiModelProperty("币种，默认 USD")
	private String currency;

	@ApiModelProperty("最近一次余额同步时间")
	private Date balanceSyncTime;

	@ApiModelProperty("KYC 本地状态 1待认证 2认证中 3成功 4失败")
	private Integer kycState;

	@ApiModelProperty("KYC 状态文案")
	private String kycStateName;

	@ApiModelProperty("三方 KYC 原始状态")
	private String kycApiStatus;

	@ApiModelProperty("是否已开卡 0否 1是")
	private Integer activationState;

	@ApiModelProperty("是否已绑定支付密码")
	private Boolean payPasswordSet;

	@ApiModelProperty("TRON USDT 充值地址")
	private String tronUsdtAddress;

	@ApiModelProperty("钱包用户状态 1正常 0禁用")
	private Integer status;

	@ApiModelProperty("开通时间")
	private Date setTime;
}
