package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 查询 Pin 结果（AES 密文）。
 */
@Data
@ApiModel(value = "查询Pin结果", description = "POST /api/bankcard/queryPin data")
public class ThirdBankcardPinResp {

	@ApiModelProperty("pin（已 AES 加密）")
	private String pin;
}
