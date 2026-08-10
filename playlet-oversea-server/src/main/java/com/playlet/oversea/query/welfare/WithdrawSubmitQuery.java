package com.playlet.oversea.query.welfare;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("提现提交请求")
public class WithdrawSubmitQuery {

	@NotBlank(message = "币种不能为空")
	@ApiModelProperty(name = "assetCode", value = "币种 TRX/USDT/USTC", required = true, dataType = "String")
	private String assetCode;

	@NotBlank(message = "网络不能为空")
	@ApiModelProperty(name = "network", value = "网络 TRC20/ERC20", required = true, dataType = "String")
	private String network;

	@NotBlank(message = "收款地址不能为空")
	@ApiModelProperty(name = "walletAddress", value = "收款地址", required = true, dataType = "String")
	private String walletAddress;

	@NotNull(message = "提现积分不能为空")
	@Min(value = 1, message = "提现积分须大于0")
	@ApiModelProperty(name = "points", value = "提现积分", required = true, dataType = "Integer")
	private Integer points;
}
