package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 三方商户资产余额（POST /api/merchant/balance）。
 */
@Data
@ApiModel(value = "商户资产", description = "worldPay 商户可用/冻结余额")
public class MerchantAssetsResp {

	@ApiModelProperty("可用金额")
	private Double availableAmount;

	@ApiModelProperty("币种")
	private String currency;

	@ApiModelProperty("冻结金额")
	private Double frozenAmount;
}
