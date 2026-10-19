package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 会员支付成功回调/确认入参。
 */
@Data
@ApiModel(value = "会员支付确认", description = "支付成功后开通/续期会员")
public class MemberPayNotifyReqEntity {

	@ApiModelProperty(value = "业务订单号", required = true)
	private String orderNo;

	@ApiModelProperty(value = "三方支付单号")
	private String thirdTradeNo;
}
