package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 现金解锁剧集下单入参。
 */
@Data
@ApiModel("现金解锁下单")
public class EpisodeUnlockCashCreateReqEntity {

	@ApiModelProperty(value = "集ID drama_asset.id", required = true)
	private Integer assetId;

	@ApiModelProperty(value = "支付渠道 WECHAT/ALIPAY/BANK", required = true)
	private String payChannel;

	@ApiModelProperty(value = "用户持券ID；可选，须为 YUAN 币种且适用 COIN_CONSUME/ALL")
	private Integer userCouponId;
}
