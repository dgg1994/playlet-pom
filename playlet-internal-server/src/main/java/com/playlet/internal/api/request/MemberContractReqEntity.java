package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 连续包月签约入参。
 */
@Data
@ApiModel("连续包月签约")
public class MemberContractReqEntity {

	@ApiModelProperty(value = "会员套餐ID，须为连续包月套餐", required = true)
	private Integer productId;

	@ApiModelProperty(value = "签约渠道 WECHAT/ALIPAY", required = true)
	private String payChannel;

	@ApiModelProperty(value = "签约场景：PC/H5/APP/MINI；默认 PC")
	private String payScene;

	@ApiModelProperty(value = "微信 MINI/JSAPI 场景的用户 openid", required = false)
	private String openId;

	@ApiModelProperty(value = "用户持券ID；可选，用于首期优惠")
	private Integer userCouponId;
}
