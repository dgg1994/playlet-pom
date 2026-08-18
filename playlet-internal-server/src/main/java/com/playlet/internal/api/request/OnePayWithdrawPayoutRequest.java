package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 向 OnePay 发起打款的请求体。
 */
@Data
@ApiModel("OnePay打款请求")
public class OnePayWithdrawPayoutRequest {

	@ApiModelProperty("本系统提现单号")
	private String orderNo;

	@ApiModelProperty("OnePay 账号")
	private String onePayAccount;

	@ApiModelProperty("OnePay 侧用户ID")
	private String onePayOpenId;

	@ApiModelProperty("提现金币数量")
	private Integer points;
}
