package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 作家收益管理首页概览。
 */
@Data
@ApiModel(value = "作家收益概览", description = "今日/累计/待结算金币及 OnePay 结算账户")
public class CreatorRevenueSummaryRespEntity {

	@ApiModelProperty("今日收益（金币）")
	private Long todayIncomeCoin;

	@ApiModelProperty("累计收益（金币）")
	private Long totalIncomeCoin;

	@ApiModelProperty("待结算收益（金币）")
	private Long pendingSettleCoin;

	@ApiModelProperty("当前结算账户")
	private CreatorSettlementAccountRespEntity settlementAccount;

	@ApiModelProperty("近 7 日收益趋势（含今天，无收益日为 0）")
	private List<CreatorRevenueTrendItemRespEntity> incomeTrend = new ArrayList<>();
}
