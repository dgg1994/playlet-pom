package com.playlet.oversea.api.response;

import lombok.Data;

/**
 * worldPay 邮寄地址三方响应 data。
 */
@Data
public class ThirdMailingAddressResp {

	private Integer id;

	private Integer countryRegionId;

	private String country;

	private String city;

	private String receiverName;

	private String receiverMobile;

	private String receiverAddress;

	private String postCode;
}
