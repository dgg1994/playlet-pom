package com.playlet.internal.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 钱包账户（KYC / 开卡状态 / 账户余额缓存）。
 */
@Data
@TableName("wallet_account")
@ApiModel(value = "钱包账户", description = "KYC、开卡状态与账户余额缓存（权威以三方为准）")
public class WalletAccountEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("wallet_user_id")
	@ApiModelProperty(name = "walletUserId", value = "wallet_user.id")
	private Long walletUserId;

	@TableField("wallet_uid")
	@ApiModelProperty(name = "walletUid", value = "钱包三方 uid")
	private Long walletUid;

	@TableField("kyc_state")
	@ApiModelProperty(name = "kycState", value = "1待认证 2认证中 3成功 4失败")
	private Integer kycState;

	@TableField("kyc_state_name")
	@ApiModelProperty(name = "kycStateName", value = "KYC状态文案")
	private String kycStateName;

	@TableField("kyc_api_status")
	@ApiModelProperty(name = "kycApiStatus", value = "三方原始状态")
	private String kycApiStatus;

	@TableField("kyc_audit_result")
	@ApiModelProperty(name = "kycAuditResult", value = "KYC结果描述")
	private String kycAuditResult;

	@TableField("activation_state")
	@ApiModelProperty(name = "activationState", value = "是否已开卡：0否 1是")
	private Integer activationState;

	@TableField("activation_time")
	@ApiModelProperty(name = "activationTime", value = "首次开卡激活时间")
	private Date activationTime;

	@TableField("available_balance")
	@ApiModelProperty(name = "availableBalance", value = "账户可用余额缓存")
	private BigDecimal availableBalance;

	@TableField("freeze_balance")
	@ApiModelProperty(name = "freezeBalance", value = "冻结余额缓存")
	private BigDecimal freezeBalance;

	@TableField("open_freeze_balance")
	@ApiModelProperty(name = "openFreezeBalance", value = "开卡临时冻结缓存")
	private BigDecimal openFreezeBalance;

	@TableField("currency")
	@ApiModelProperty(name = "currency", value = "币种，默认 USD")
	private String currency;

	@TableField("balance_sync_time")
	@ApiModelProperty(name = "balanceSyncTime", value = "最近一次余额同步时间")
	private Date balanceSyncTime;

	@TableField("pay_password")
	@ApiModelProperty(name = "payPassword", value = "支付密码哈希，不对外返回")
	@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	@com.alibaba.fastjson.annotation.JSONField(serialize = false)
	private String payPassword;

	@TableField("pay_password_set_time")
	@ApiModelProperty(name = "payPasswordSetTime", value = "支付密码绑定时间")
	private Date payPasswordSetTime;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
