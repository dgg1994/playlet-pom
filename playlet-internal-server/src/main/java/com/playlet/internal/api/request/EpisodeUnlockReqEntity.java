package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 剧集解锁入参（金币 / 现金 统一入口）。
 */
@Data
@ApiModel("剧集解锁")
public class EpisodeUnlockReqEntity {

	@ApiModelProperty(value = "集ID drama_asset.id", required = true)
	private Integer assetId;

	@ApiModelProperty(value = "支付方式 COIN=金币（立即解锁）/ CASH=现金（下单待支付）", required = true)
	private String payType;

	@ApiModelProperty(value = "解锁范围：SINGLE=仅当前集（默认）；REMAINING=当前用户所有未解锁的付费集一次性解锁")
	private String unlockScope;

	@ApiModelProperty(value = "用户持券ID；可选。payType=COIN 须为 COIN 币种，payType=CASH 须为 YUAN 币种，且适用 COIN_CONSUME/ALL")
	private Integer userCouponId;

	@ApiModelProperty(value = "支付渠道 WECHAT/ALIPAY/BANK；payType=CASH 时必填")
	private String payChannel;

	@ApiModelProperty(value = "支付场景：PC/H5/APP/MINI；默认 PC，payType=CASH 时生效")
	private String payScene;

	@ApiModelProperty(value = "微信 JSAPI/小程序场景的用户 openid；payChannel=WECHAT 且 payScene=MINI 时必填")
	private String openId;
}
