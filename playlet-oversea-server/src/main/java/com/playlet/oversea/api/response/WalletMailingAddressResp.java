package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 用户收件地址返回（id 为三方邮寄地址 id，可用于 deliveryAddressId）。
 */
@Data
@ApiModel(value = "用户收件地址", description = "邮寄地址详情")
public class WalletMailingAddressResp {

	@ApiModelProperty(value = "三方邮寄地址 id（deliveryAddressId）")
	private Integer id;

	@ApiModelProperty(value = "邮寄地区 id")
	private Integer countryRegionId;

	@ApiModelProperty(value = "国家")
	private String country;

	@ApiModelProperty(value = "城市")
	private String city;

	@ApiModelProperty(value = "收件人")
	private String receiverName;

	@ApiModelProperty(value = "收件人电话")
	private String receiverMobile;

	@ApiModelProperty(value = "邮寄地址")
	private String receiverAddress;

	@ApiModelProperty(value = "邮编")
	private String postCode;

	@ApiModelProperty(value = "创建时间")
	private Date setTime;

	@ApiModelProperty(value = "更新时间")
	private Date gmtModified;
}
