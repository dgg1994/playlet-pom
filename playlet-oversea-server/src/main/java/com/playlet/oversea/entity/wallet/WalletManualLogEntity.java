package com.playlet.oversea.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端人工充值审计日志。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_manual_log")
@ApiModel(value = "人工充值日志", description = "后台 walletTopUp 操作记录")
public class WalletManualLogEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("order_no")
	private String orderNo;

	@TableField("wallet_user_id")
	private Long walletUserId;

	@TableField("wallet_uid")
	private Long walletUid;

	@TableField("local_uid")
	private Integer localUid;

	@TableField("user_email")
	private String userEmail;

	@TableField("user_tel")
	private String userTel;

	@TableField("wallet_type")
	private String walletType;

	@TableField("wallet_address")
	private String walletAddress;

	@TableField("topup_amount")
	private BigDecimal topupAmount;

	@TableField("topup_amount_forward")
	private BigDecimal topupAmountForward;

	@TableField("topup_amount_back")
	private BigDecimal topupAmountBack;

	@TableField("operate_user_id")
	private Integer operateUserId;

	@TableField("operate_user_name")
	private String operateUserName;

	@TableField("operate_user_ip")
	private String operateUserIp;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;

	/** 管理端入参：C 端 uid（app_account.id） */
	@TableField(exist = false)
	private String uid;

	/** 管理端入参：充值金额 */
	@TableField(exist = false)
	private Double topupAmountInput;
}
