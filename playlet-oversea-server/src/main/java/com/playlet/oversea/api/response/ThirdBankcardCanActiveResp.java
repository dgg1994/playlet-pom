package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 银行卡是否可激活结果。
 */
@Data
@ApiModel(value = "是否可激活", description = "POST /api/bankcard/get/canActive data")
public class ThirdBankcardCanActiveResp {

	@ApiModelProperty("是否可激活")
	private Boolean canActive;
}
