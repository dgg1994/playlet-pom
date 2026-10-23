package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端充值订单顶部统计。
 */
@Data
@ApiModel(value = "充值订单统计", description = "今日交易额/订单数/退款率/待结算")
public class RechargeOrderSummaryEntity {

	@ApiModelProperty("今日交易额(元，已支付)")
	private BigDecimal todayAmount;

	@ApiModelProperty("今日订单数（含各状态）")
	private Long todayOrderCount;

	@ApiModelProperty("退款率(%)，今日：退款数/(已支付+已退款)")
	private BigDecimal refundRate;

	@ApiModelProperty("待结算金额(元)：已支付未退款累计")
	private BigDecimal pendingSettleAmount;
}
