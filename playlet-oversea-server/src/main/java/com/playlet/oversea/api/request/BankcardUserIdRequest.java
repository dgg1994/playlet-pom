package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 仅含 userBankcardId 的卡片操作入参。
 */
@Data
@ApiModel(value = "银行卡id入参", description = "余额/信息/注销/查Pin 等")
public class BankcardUserIdRequest {

	@ApiModelProperty(value = "银行卡id", required = true)
	private Long userBankcardId;
}
