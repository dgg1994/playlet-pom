package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 更新邮寄地址（对齐 worldPay POST /api/delivery/address/update）。
 */
@Data
@ApiModel(value = "更新邮寄地址", description = "POST /wallet/mailing/update")
public class WalletMailingAddressUpdateRequest {

	@ApiModelProperty(value = "三方邮寄地址 id（添加时返回的 id / deliveryAddressId）", required = true)
	private Integer id;

	@ApiModelProperty(value = "邮寄地区 id", required = true)
	private Integer countryRegionId;

	@ApiModelProperty(value = "国家", required = true)
	private String country;

	@ApiModelProperty(value = "城市", required = true)
	private String city;

	@ApiModelProperty(value = "收件人", required = true)
	private String receiverName;

	@ApiModelProperty(value = "收件人电话", required = true)
	private String receiverMobile;

	@ApiModelProperty(value = "邮寄地址", required = true)
	private String receiverAddress;

	@ApiModelProperty(value = "邮编", required = true)
	private String postCode;
}
