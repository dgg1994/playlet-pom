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
 * 链上提现申请/记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_to_web_log")
@ApiModel(value = "链上提现记录", description = "用户提现到 Web3 地址申请")
public class WalletToWebLogEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("wallet_user_id")
	private Long walletUserId;

	@TableField("wallet_uid")
	private Long walletUid;

	@TableField("local_uid")
	private Integer localUid;

	@TableField("order_no")
	private String orderNo;

	@TableField("user_email")
	private String userEmail;

	@TableField("user_tel")
	private String userTel;

	@TableField("network_type")
	private String networkType;

	@TableField("token_name")
	private String tokenName;

	@TableField("token_address")
	private String tokenAddress;

	@TableField("wallet_address")
	private String walletAddress;

	@TableField("transfer_amount")
	private BigDecimal transferAmount;

	@TableField("server_amount")
	private BigDecimal serverAmount;

	@TableField("handling_fee")
	private BigDecimal handlingFee;

	@TableField("real_amount")
	private BigDecimal realAmount;

	@TableField("apply_state")
	private Integer applyState;

	@TableField("apply_state_name")
	private String applyStateName;

	@TableField("reject_content")
	private String rejectContent;

	@TableField("tran_hash")
	private String tranHash;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
