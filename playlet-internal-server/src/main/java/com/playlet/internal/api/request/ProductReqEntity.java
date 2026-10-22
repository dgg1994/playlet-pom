package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 会员下单入参。
 */
@Data
@ApiModel(value = "会员下单", description = "创建会员订单入参")
public class ProductReqEntity {

	@ApiModelProperty(value = "套餐ID", required = true)
	private Integer productId;

	@ApiModelProperty(value = "支付类型：COIN 金币 / CASH 现金；默认 CASH", required = true)
	private String payType;

	@ApiModelProperty(value = "现金渠道：WECHAT / ALIPAY / BANK；payType=CASH 时必填")
	private String payChannel;

	@ApiModelProperty(value = "用户持券ID（user_coupon.id）；可选，不传则原价")
	private Integer userCouponId;
}
