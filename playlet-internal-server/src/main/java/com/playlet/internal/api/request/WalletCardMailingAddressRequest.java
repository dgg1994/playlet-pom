package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 实体卡邮寄地址（对齐 worldpay CardApplySendEntity）。
 */
@Data
@ApiModel(value = "开卡邮寄地址", description = "实体卡申请邮寄地址")
public class WalletCardMailingAddressRequest {

	@ApiModelProperty(value = "国家", required = true)
	private String nation;

	@ApiModelProperty(value = "省/州")
	private String province;

	@ApiModelProperty(value = "市")
	private String city;

	@ApiModelProperty(value = "详细地址", required = true)
	private String addressInfo;

	@ApiModelProperty(value = "收件人", required = true)
	private String collectMan;

	@ApiModelProperty(value = "收件人电话", required = true)
	private String collectTel;

	@ApiModelProperty(value = "邮编")
	private String postCode;

	@ApiModelProperty(value = "三方邮寄地址 id（与 deliveryAddressId 二选一或同时传）")
	private Integer addressId;
}
