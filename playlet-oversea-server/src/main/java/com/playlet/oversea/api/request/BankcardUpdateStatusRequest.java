package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 更新银行卡状态（冻结/解冻）入参。
 */
@Data
@ApiModel(value = "更新银行卡状态", description = "POST /api/bankcard/update/status")
public class BankcardUpdateStatusRequest {

	@ApiModelProperty(value = "银行卡id", required = true)
	private Long userBankcardId;

	@ApiModelProperty(value = "true解冻 false冻结", required = true)
	private Boolean enable;
}
