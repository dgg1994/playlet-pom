package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创作者首页顶部数据概览。
 */
@Data
@ApiModel(value = "创作者首页概览", description = "收益/余额/播放/在播；金额字段存金币数值")
public class CreatorHomeStatsRespEntity {

	@ApiModelProperty("今日收益（金币）")
	private BigDecimal todayIncomeYuan;

	@ApiModelProperty("昨日收益（金币）")
	private BigDecimal yesterdayIncomeYuan;

	@ApiModelProperty("当前可用余额（金币）= coin_balance - frozen")
	private BigDecimal balanceYuan;

	@ApiModelProperty("累计收益（金币）")
	private BigDecimal totalIncomeYuan;

	@ApiModelProperty("今日播放量（所属剧 play_pv 合计）")
	private Long todayPlayCount;

	@ApiModelProperty("昨日播放量")
	private Long yesterdayPlayCount;

	@ApiModelProperty("在播短剧数（已上架）")
	private Integer onAirDramaCount;

	@ApiModelProperty("在播剧集数（已上架集）")
	private Integer onAirEpisodeCount;
}
