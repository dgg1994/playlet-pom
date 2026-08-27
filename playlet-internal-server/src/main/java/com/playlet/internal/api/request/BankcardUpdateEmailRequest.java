package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 更新银行卡邮箱入参。
 */
@Data
@ApiModel(value = "更新银行卡邮箱", description = "POST /api/bankcard/update/email")
public class BankcardUpdateEmailRequest {

	@ApiModelProperty(value = "银行卡id", required = true)
	private Long userBankcardId;

	@ApiModelProperty(value = "邮箱地址", required = true)
	private String email;
}
