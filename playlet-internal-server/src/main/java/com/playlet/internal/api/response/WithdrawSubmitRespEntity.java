package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("提现提交结果")
public class WithdrawSubmitRespEntity {

	@ApiModelProperty("业务单号")
	private String orderNo;

	@ApiModelProperty("币种")
	private String assetCode;

	@ApiModelProperty("网络")
	private String network;

	@ApiModelProperty("订单状态")
	private Integer status;

	@ApiModelProperty("扣减积分")
	private Integer pointsAmt;

	@ApiModelProperty("实到金额")
	private String actualAmt;

	@ApiModelProperty("冻结后可用金币")
	private Long coinBalance;

	@ApiModelProperty("冻结金币")
	private Long frozenCoinBalance;
}
