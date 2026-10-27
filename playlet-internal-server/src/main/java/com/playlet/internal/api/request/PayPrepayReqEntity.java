package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 现金订单重新获取支付参数入参。
 */
@Data
@ApiModel("重新发起支付")
public class PayPrepayReqEntity {

	@ApiModelProperty(value = "业务订单号（会员单/单集解锁单）", required = true)
	private String orderNo;

	@ApiModelProperty(value = "支付场景：PC/H5/APP/MINI；默认 PC", required = false)
	private String payScene;

	@ApiModelProperty(value = "微信 JSAPI/小程序场景所需的用户 openid", required = false)
	private String openId;
}
