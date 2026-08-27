package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * worldPay KYC 状态查询结果。
 */
@Data
@ApiModel(value = "KYC状态", description = "data.status / failedReason")
public class KycStatusResp {

	@ApiModelProperty("KYC状态：uncommitted/waiting/success/fail/wait_confirm/wait_audit")
	private String status;

	@ApiModelProperty("失败原因")
	private String failedReason;
}
