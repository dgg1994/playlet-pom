package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理端充值订单退款入参。
 */
@Data
@ApiModel(value = "充值订单退款", description = "标记已退款（暂不对接渠道退款）")
public class RechargeOrderRefundQuery {

	@ApiModelProperty(value = "订单号", required = true)
	private String orderNo;

	@ApiModelProperty(value = "商品类型 MEMBER / EPISODE", required = true)
	private String productType;

	@ApiModelProperty("备注")
	private String remark;
}
