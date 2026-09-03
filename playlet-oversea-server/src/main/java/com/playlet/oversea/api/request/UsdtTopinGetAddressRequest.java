package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 向 USDT 充值网关申请地址。
 */
@Data
@ApiModel(value = "USDT充值地址申请", description = "透传三方 /listen/getaddress")
public class UsdtTopinGetAddressRequest {

	@ApiModelProperty("钱包三方 uid")
	private String uid;

	@ApiModelProperty("用户邮箱")
	private String email;

	@ApiModelProperty("签名")
	private String sign;
}
