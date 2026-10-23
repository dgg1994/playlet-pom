package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 运营看板趋势点（充值 + 播放）。
 */
@Data
@ApiModel(value = "运营看板趋势点", description = "按日充值金额与播放量")
public class OpsDashboardTrendPoint {

	@ApiModelProperty("业务日 yyyy-MM-dd")
	private String bizDate;

	@ApiModelProperty("当日展示用短日期 MM-dd")
	private String label;

	@ApiModelProperty("充值金额(元)")
	private BigDecimal rechargeAmount;

	@ApiModelProperty("播放次数（次）")
	private Long playCount;

	@ApiModelProperty("播放量(万次，一位小数)")
	private BigDecimal playCountWan;
}
