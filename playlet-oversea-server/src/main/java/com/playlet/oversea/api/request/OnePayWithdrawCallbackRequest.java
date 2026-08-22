package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * OnePay 打款结果回调。
 */
@Data
@ApiModel("OnePay提现回调")
public class OnePayWithdrawCallbackRequest {

	@ApiModelProperty(value = "本系统提现单号", required = true)
	private String orderNo;

	@ApiModelProperty(value = "1成功 0失败", required = true)
	private Integer success;

	@ApiModelProperty("三方流水号")
	private String thirdOrderNo;

	@ApiModelProperty("失败原因")
	private String failReason;
}
