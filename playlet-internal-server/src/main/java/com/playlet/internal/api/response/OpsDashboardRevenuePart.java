package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 运营看板收入构成项。
 */
@Data
@ApiModel(value = "收入构成项", description = "单集/会员/金币/广告等收入占比")
public class OpsDashboardRevenuePart {

	@ApiModelProperty("类型码 EPISODE / MEMBER / COIN / AD")
	private String type;

	@ApiModelProperty("展示名")
	private String name;

	@ApiModelProperty("金额(元)")
	private BigDecimal amount;

	@ApiModelProperty("占比%(一位小数)")
	private BigDecimal rate;
}
