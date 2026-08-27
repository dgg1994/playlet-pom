package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户卡列表项（三方）。
 */
@Data
@ApiModel(value = "用户卡", description = "GET /api/bankcard/user/card/list")
public class ThirdUserBankcardResp {

	@ApiModelProperty("卡id")
	private Long userBankcardId;

	@ApiModelProperty("VIRTUAL / PHYSICAL")
	private String cardType;

	@ApiModelProperty("产品id")
	private Integer productId;

	@ApiModelProperty("卡号")
	private String cardNumber;

	@ApiModelProperty("卡状态 0-9")
	private Integer status;

	@ApiModelProperty("币种")
	private String currency;
}
