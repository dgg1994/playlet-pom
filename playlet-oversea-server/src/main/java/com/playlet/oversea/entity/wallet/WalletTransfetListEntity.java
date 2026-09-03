package com.playlet.oversea.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 钱包内部转账记录。
 */
@Data
@TableName("wallet_transfet_list")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "钱包内部转账记录", description = "用户间内部转账流水")
public class WalletTransfetListEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("order_no")
	@ApiModelProperty(name = "orderNo", value = "订单号")
	private String orderNo;

	@TableField("send_wallet_uid")
	@ApiModelProperty(name = "sendWalletUid", value = "发送方 wallet_uid")
	private Long sendWalletUid;

	@TableField("send_email")
	@ApiModelProperty(name = "sendEmail", value = "发送方邮箱")
	private String sendEmail;

	@TableField("send_forward_balance")
	@ApiModelProperty(name = "sendForwardBalance", value = "发送前余额")
	private BigDecimal sendForwardBalance;

	@TableField("send_back_balance")
	@ApiModelProperty(name = "sendBackBalance", value = "发送后余额")
	private BigDecimal sendBackBalance;

	@TableField("recipient_wallet_uid")
	@ApiModelProperty(name = "recipientWalletUid", value = "接收方 wallet_uid")
	private Long recipientWalletUid;

	@TableField("recipient_email")
	@ApiModelProperty(name = "recipientEmail", value = "接收方邮箱（转账入参）")
	private String recipientEmail;

	@TableField("recipient_forward_balance")
	@ApiModelProperty(name = "recipientForwardBalance", value = "接收前余额")
	private BigDecimal recipientForwardBalance;

	@TableField("recipient_back_balance")
	@ApiModelProperty(name = "recipientBackBalance", value = "接收后余额")
	private BigDecimal recipientBackBalance;

	@TableField("send_money")
	@ApiModelProperty(name = "sendMoney", value = "发送金额")
	private BigDecimal sendMoney;

	@TableField("send_rates")
	@ApiModelProperty(name = "sendRates", value = "费率")
	private BigDecimal sendRates;

	@TableField("actual_money")
	@ApiModelProperty(name = "actualMoney", value = "实际到账")
	private BigDecimal actualMoney;

	@TableField("handling_fee")
	@ApiModelProperty(name = "handlingFee", value = "手续费")
	private BigDecimal handlingFee;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;

	@TableField(exist = false)
	@ApiModelProperty(name = "payPassword", value = "支付密码")
	private String payPassword;

	@TableField(exist = false)
	@ApiModelProperty(name = "contactsLabel", value = "通讯录标签")
	private String contactsLabel;

	/** 兼容 onetoken 字段名：sendUid */
	@TableField(exist = false)
	@ApiModelProperty(name = "sendUid", value = "发送方 uid（兼容）")
	private Long sendUid;

}
