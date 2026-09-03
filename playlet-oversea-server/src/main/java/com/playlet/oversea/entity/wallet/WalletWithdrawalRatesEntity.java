package com.playlet.oversea.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 链上提现费率配置。
 */
@Data
@TableName("wallet_withdrawal_rates")
@ApiModel(value = "提现费率", description = "链上提现手续费与服务费")
public class WalletWithdrawalRatesEntity {

	@TableId(type = IdType.AUTO)
	private Integer id;

	@TableField("min_amount")
	private BigDecimal minAmount;

	@TableField("max_amount")
	private BigDecimal maxAmount;

	@TableField("handling_rates")
	private BigDecimal handlingRates;

	@TableField("server_amount")
	private BigDecimal serverAmount;

	@TableField("set_user")
	private Integer setUser;

	@TableField("set_user_name")
	private String setUserName;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
