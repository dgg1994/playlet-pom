package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * C 端 / 作家端 KYC 状态（三方状态 + 本地映射）。
 */
@Data
@ApiModel(value = "钱包KYC状态", description = "查询 KYC 状态出参")
public class WalletKycStatusResp {

	@ApiModelProperty("三方状态：uncommitted/waiting/success/fail/wait_confirm/wait_audit")
	private String status;

	@ApiModelProperty("失败原因")
	private String failedReason;

	@ApiModelProperty("本地状态：1待认证 2认证中 3成功 4失败")
	private Integer kycState;

	@ApiModelProperty("本地状态文案")
	private String kycStateName;
}
