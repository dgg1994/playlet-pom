package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * C 端金币余额与汇率。
 */
@Data
@ApiModel("金币余额")
public class CoinBalanceRespEntity {

	@ApiModelProperty("金币总余额")
	private Long coinBalance;

	@ApiModelProperty("冻结金币")
	private Long frozenCoinBalance;

	@ApiModelProperty("可用金币 = 总余额 - 冻结")
	private Long availableCoin;

	@ApiModelProperty("当前多少币≈1元；无配置为 null")
	private Integer coinPerYuan;

	@ApiModelProperty("汇率模式 1手动 2按日")
	private Integer rateMode;
}
