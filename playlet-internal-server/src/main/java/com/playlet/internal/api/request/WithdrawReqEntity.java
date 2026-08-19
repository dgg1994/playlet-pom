package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 提现入参：金币提现到已绑定的 OnePay 账户。
 */
@Data
@ApiModel("提现请求")
public class WithdrawReqEntity {

	@NotNull(message = "提现金币不能为空")
	@Min(value = 1, message = "提现金币须大于0")
	@ApiModelProperty(value = "提现金币数量", required = true)
	private Integer points;
}
