package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 作家收益趋势单日数据点。
 */
@Data
@ApiModel(value = "作家收益趋势项", description = "近 N 日按自然日聚合")
public class CreatorRevenueTrendItemRespEntity {

	@ApiModelProperty("日期 yyyy-MM-dd")
	private String date;

	@ApiModelProperty("当日收益（金币）")
	private Long incomeCoin;
}
