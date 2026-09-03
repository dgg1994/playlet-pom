package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * USDT 充值地址列表项（对齐 worldpay UserAddressReq）。
 */
@Data
@ApiModel(value = "USDT充值地址项", description = "按链类型返回充值地址")
public class WalletTopinAddressItemResp {

	@ApiModelProperty("链类型，如 TRON / BSC")
	private String addressType;

	@ApiModelProperty("充值地址")
	private String address;

	@ApiModelProperty("充值说明文案")
	private String countInfo;

	@ApiModelProperty("充值说明链接")
	private String countUrl;
}
