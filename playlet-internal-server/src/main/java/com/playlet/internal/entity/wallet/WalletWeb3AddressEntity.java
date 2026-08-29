package com.playlet.internal.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Web3 链上充值地址缓存。
 */
@Data
@TableName("wallet_web3_address")
@ApiModel(value = "Web3充值地址", description = "多链充值地址缓存")
public class WalletWeb3AddressEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("wallet_user_id")
	@ApiModelProperty("wallet_user.id")
	private Long walletUserId;

	@TableField("wallet_uid")
	@ApiModelProperty("钱包三方 uid")
	private Long walletUid;

	@TableField("user_email")
	@ApiModelProperty("用户邮箱")
	private String userEmail;

	@TableField("tron_address")
	@ApiModelProperty("TRON 地址")
	private String tronAddress;

	@TableField("bnb_address")
	@ApiModelProperty("BNB/BSC 地址")
	private String bnbAddress;

	@TableField("eth_address")
	@ApiModelProperty("ETH 地址")
	private String ethAddress;

	@TableField("btc_address")
	@ApiModelProperty("BTC 地址")
	private String btcAddress;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
