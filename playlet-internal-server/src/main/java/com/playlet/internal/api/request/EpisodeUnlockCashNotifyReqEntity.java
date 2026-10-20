package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 现金解锁支付确认入参。
 */
@Data
@ApiModel("现金解锁支付确认")
public class EpisodeUnlockCashNotifyReqEntity {

	@ApiModelProperty(value = "业务订单号", required = true)
	private String orderNo;

	@ApiModelProperty("三方支付单号")
	private String thirdTradeNo;
}
