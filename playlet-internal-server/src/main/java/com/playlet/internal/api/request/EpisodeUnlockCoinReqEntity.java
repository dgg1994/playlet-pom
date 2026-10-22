package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 金币解锁剧集入参。
 */
@Data
@ApiModel("金币解锁剧集")
public class EpisodeUnlockCoinReqEntity {

	@ApiModelProperty(value = "集ID drama_asset.id", required = true)
	private Integer assetId;

	@ApiModelProperty(value = "用户持券ID；可选，须为 COIN 币种且适用 COIN_CONSUME/ALL")
	private Integer userCouponId;
}
