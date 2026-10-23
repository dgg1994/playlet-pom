package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * C端/管理端提交退款申请入参。
 */
@Data
@ApiModel(value = "退款申请提交", description = "用户对已支付订单发起退款申请")
public class RefundApplyCreateQuery {

	@ApiModelProperty(value = "原订单号", required = true)
	private String orderNo;

	@ApiModelProperty(value = "商品类型 MEMBER / EPISODE", required = true)
	private String productType;

	@ApiModelProperty(value = "退款原因", required = true)
	private String refundReason;
}
