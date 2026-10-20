package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 广告解锁剧集入参（App 看完激励广告后上报）。
 */
@Data
@ApiModel("广告解锁剧集")
public class EpisodeUnlockAdReqEntity {

	@ApiModelProperty(value = "集ID drama_asset.id", required = true)
	private Integer assetId;

	@ApiModelProperty(value = "广告平台交易号（幂等）", required = true)
	private String transId;

	@ApiModelProperty("广告位业务码，默认 REWARD_UNLOCK")
	private String slotCode;

	@ApiModelProperty("本次 eCPM（元/千次），可选")
	private BigDecimal ecpm;
}
