package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * USDT 充值地址返回。
 */
@Data
@ApiModel(value = "USDT充值地址", description = "TRON USDT 充值地址")
public class UsdtTopinAddressResp {

	@ApiModelProperty("TRON USDT 充值地址")
	private String address;

	@ApiModelProperty("网络类型")
	private String network;

	public static UsdtTopinAddressResp of(String address) {
		UsdtTopinAddressResp resp = new UsdtTopinAddressResp();
		resp.setAddress(address);
		resp.setNetwork("TRON");
		return resp;
	}
}
