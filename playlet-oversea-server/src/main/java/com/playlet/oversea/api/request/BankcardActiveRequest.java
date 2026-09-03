package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 银行卡激活入参。
 */
@Data
@ApiModel(value = "银行卡激活", description = "POST /api/bankcard/active")
public class BankcardActiveRequest {

	@ApiModelProperty(value = "产品id", required = true)
	private Integer productId;

	@ApiModelProperty(value = "卡号", required = true)
	private String cardNo;

	@ApiModelProperty(value = "激活码")
	private String verifyCode;

	@ApiModelProperty(value = "手机区号", required = true)
	private String mobilePrefix;

	@ApiModelProperty(value = "手机号", required = true)
	private String mobile;

	@ApiModelProperty(value = "国家编码", required = true)
	private String countryCode;

	@ApiModelProperty(value = "地址1", required = true)
	private String address;

	@ApiModelProperty(value = "地址2")
	private String address2;

	@ApiModelProperty(value = "城市", required = true)
	private String city;

	@ApiModelProperty(value = "省/州", required = true)
	private String state;

	@ApiModelProperty(value = "邮编", required = true)
	private String postCode;
}
