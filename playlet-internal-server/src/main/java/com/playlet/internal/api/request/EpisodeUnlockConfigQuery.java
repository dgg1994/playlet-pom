package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端：单集/批量解锁配置入参。
 */
@Data
@ApiModel(value = "剧集解锁配置", description = "单集或按集序号批量配置解锁类型与价格")
public class EpisodeUnlockConfigQuery {

	@ApiModelProperty(value = "集ID（单集配置必填）")
	private Integer assetId;

	@ApiModelProperty(value = "短剧ID（批量配置必填）")
	private Integer dramaId;

	@ApiModelProperty(value = "从该集序号起批量配置（含）")
	private Integer fromSetNum;

	@ApiModelProperty(value = "0免费 1广告 2付费", required = true)
	private Integer unlockType;

	@ApiModelProperty(value = "金币价；付费时可填")
	private Integer priceCoin;

	@ApiModelProperty(value = "现金价(元)；付费时可填")
	private BigDecimal priceYuan;
}
