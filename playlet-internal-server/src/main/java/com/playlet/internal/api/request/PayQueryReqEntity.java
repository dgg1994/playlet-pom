package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 渠道订单查询入参。
 */
@Data
@ApiModel("支付订单查询")
public class PayQueryReqEntity {

	@ApiModelProperty(value = "业务订单号（会员单/单集解锁单）", required = true)
	private String orderNo;
}
