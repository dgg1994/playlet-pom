package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 注销银行卡入参。
 */
@Data
@ApiModel(value = "注销银行卡", description = "POST /wallet/card/close")
public class BankcardCloseRequest {

	@ApiModelProperty(value = "银行卡id", required = true)
	private Long userBankcardId;

	@ApiModelProperty(value = "支付密码（6 位数字；须已绑定）", required = true)
	private String payPassword;
}
