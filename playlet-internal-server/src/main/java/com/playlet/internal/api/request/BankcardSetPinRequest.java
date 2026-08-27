package com.playlet.internal.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 设置银行卡 Pin 入参。
 */
@Data
@ApiModel(value = "设置Pin", description = "POST /api/bankcard/setPin")
public class BankcardSetPinRequest {

	@ApiModelProperty(value = "用户银行卡id", required = true)
	private Long userBankcardId;

	@ApiModelProperty(value = "pin码", required = true)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String pin;
}
