package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 钱包交易记录列表项。
 */
@Data
@ApiModel(value = "钱包交易列表项", description = "最近交易/全部交易展示字段")
public class WalletTransactionItemResp {

	@ApiModelProperty("流水 id")
	private Long id;

	@ApiModelProperty("本地卡 id")
	private Long walletBankcardId;

	@ApiModelProperty("展示标题，如 提现 (TRC20)")
	private String title;

	@ApiModelProperty("业务类型 APPLY/RECHARGE/WITHDRAW/AUTH/REFUND/CLOSE")
	private String bizType;

	@ApiModelProperty("交易类型 TOPUP/AUTH/REFUND/CLOSE")
	private String transType;

	@ApiModelProperty("订单状态：1处理中 2成功 3失败")
	private Integer orderState;

	@ApiModelProperty("状态文案，如 已拒绝")
	private String orderStateName;

	@ApiModelProperty("展示金额（支出为负，入账为正）")
	private BigDecimal amount;

	@ApiModelProperty("币种")
	private String currency;

	@ApiModelProperty("卡号掩码")
	private String cardNo;

	@ApiModelProperty("创建时间")
	private Date setTime;
}
