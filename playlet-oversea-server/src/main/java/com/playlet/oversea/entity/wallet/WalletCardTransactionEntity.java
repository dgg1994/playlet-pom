package com.playlet.oversea.entity.wallet;

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
 * 钱包 U 卡交易流水。
 */
@Data
@TableName("wallet_card_transaction")
@ApiModel(value = "钱包卡交易", description = "开卡/充值/提现/交易流水")
public class WalletCardTransactionEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("wallet_user_id")
	@ApiModelProperty(name = "walletUserId", value = "wallet_user.id")
	private Long walletUserId;

	@TableField("wallet_uid")
	@ApiModelProperty(name = "walletUid", value = "钱包三方 uid")
	private Long walletUid;

	@TableField("wallet_bankcard_id")
	@ApiModelProperty(name = "walletBankcardId", value = "本地 wallet_bankcard.id")
	private Long walletBankcardId;

	@TableField("user_bankcard_id")
	@ApiModelProperty(name = "userBankcardId", value = "对方 userBankcardId")
	private Long userBankcardId;

	@TableField("card_product_id")
	@ApiModelProperty(name = "cardProductId", value = "卡产品 id")
	private Integer cardProductId;

	@TableField("card_uuid")
	@ApiModelProperty(name = "cardUuid", value = "卡产品 uuid")
	private String cardUuid;

	@TableField("card_no")
	@ApiModelProperty(name = "cardNo", value = "卡号掩码")
	private String cardNo;

	@TableField("request_order_id")
	@ApiModelProperty(name = "requestOrderId", value = "我方幂等单号")
	private String requestOrderId;

	@TableField("third_order_num")
	@ApiModelProperty(name = "thirdOrderNum", value = "对方 orderNum")
	private String thirdOrderNum;

	@TableField("biz_type")
	@ApiModelProperty(name = "bizType", value = "APPLY/RECHARGE/WITHDRAW/AUTH/REFUND/CLOSE")
	private String bizType;

	@TableField("trans_type")
	@ApiModelProperty(name = "transType", value = "TOPUP/AUTH/REFUND/CLOSE")
	private String transType;

	@TableField("pay_type")
	@ApiModelProperty(name = "payType", value = "1钱包 2银行卡 3人工")
	private Integer payType;

	@TableField("order_state")
	@ApiModelProperty(name = "orderState", value = "订单状态：1处理中 2成功 3失败（与 wallet_log.status 一致）")
	private Integer orderState;

	@TableField("order_state_name")
	@ApiModelProperty(name = "orderStateName", value = "状态文案")
	private String orderStateName;

	@TableField("local_currency")
	@ApiModelProperty(name = "localCurrency", value = "本地币种")
	private String localCurrency;

	@TableField("local_currency_amt")
	@ApiModelProperty(name = "localCurrencyAmt", value = "本地金额")
	private BigDecimal localCurrencyAmt;

	@TableField("trans_currency")
	@ApiModelProperty(name = "transCurrency", value = "交易币种")
	private String transCurrency;

	@TableField("trans_currency_amt")
	@ApiModelProperty(name = "transCurrencyAmt", value = "交易金额")
	private BigDecimal transCurrencyAmt;

	@TableField("handling_fees")
	@ApiModelProperty(name = "handlingFees", value = "手续费")
	private BigDecimal handlingFees;

	@TableField("withdraw_order_id")
	@ApiModelProperty(name = "withdrawOrderId", value = "关联提现单 id")
	private Long withdrawOrderId;

	@TableField("title")
	@ApiModelProperty(name = "title", value = "展示标题")
	private String title;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;

	@TableField(exist = false)
	private String userEmail;

	@TableField(exist = false)
	private String transTypeLabel;

	@TableField(exist = false)
	private java.math.BigDecimal totalManey;
}
