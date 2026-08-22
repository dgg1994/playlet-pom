package com.playlet.internal.query.ops;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 运营看板汇总查询。
 */
@Data
@ApiModel(value = "运营看板查询", description = "rangeType=today|7d|30d|custom")
public class OpsDashboardQuery {

	@ApiModelProperty(value = "区间类型：today / 7d / 30d / custom", required = true)
	private String rangeType;

	@ApiModelProperty(value = "自定义开始日 yyyy-MM-dd（rangeType=custom 必填）")
	private String startDate;

	@ApiModelProperty(value = "自定义结束日 yyyy-MM-dd（rangeType=custom 必填，含当日）")
	private String endDate;
}
