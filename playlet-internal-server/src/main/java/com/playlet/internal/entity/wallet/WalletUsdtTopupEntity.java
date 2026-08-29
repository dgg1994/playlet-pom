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
 * USDT 链上充值流水。
 */
@Data
@TableName("wallet_usdt_topup_log")
@ApiModel(value = "USDT充值流水", description = "链上充值回调落库，tx_hash 幂等")
public class WalletUsdtTopupEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("wallet_user_id")
	private Long walletUserId;

	@TableField("wallet_uid")
	private Long walletUid;

	@TableField("user_type")
	private Integer userType;

	@TableField("local_uid")
	private Integer localUid;

	@TableField("tx_hash")
	private String txHash;

	@TableField("order_no")
	private String orderNo;

	@TableField("coin")
	private String coin;

	@TableField("amount")
	private BigDecimal amount;

	@TableField("out_address")
	private String outAddress;

	@TableField("in_address")
	private String inAddress;

	@TableField("balance_before")
	private BigDecimal balanceBefore;

	@TableField("balance_after")
	private BigDecimal balanceAfter;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
