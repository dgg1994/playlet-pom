package com.playlet.internal.query.welfare;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("提现试算请求")
public class WithdrawPreviewQuery {

	@NotBlank(message = "币种不能为空")
	@ApiModelProperty(name = "assetCode", value = "币种 TRX/USDT/USTC", required = true, dataType = "String")
	private String assetCode;

	@NotBlank(message = "网络不能为空")
	@ApiModelProperty(name = "network", value = "网络 TRC20/ERC20", required = true, dataType = "String")
	private String network;

	@NotNull(message = "提现积分不能为空")
	@Min(value = 1, message = "提现积分须大于0")
	@ApiModelProperty(name = "points", value = "提现积分", required = true, dataType = "Integer")
	private Integer points;
}
