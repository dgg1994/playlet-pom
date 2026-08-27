package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 银行卡敏感信息（cvv/明文卡号等，按需展示）。
 */
@Data
@ApiModel(value = "银行卡信息", description = "POST /api/bankcard/info data")
public class ThirdBankcardInfoResp {

	@ApiModelProperty("卡cvv")
	private String cvv;

	@ApiModelProperty("0=url 1=明文")
	private Integer infoType;

	@ApiModelProperty("卡信息网页地址，infoType=0 时用")
	private String cardPanUrl;

	@ApiModelProperty("过期日期")
	private String expireDate;

	@ApiModelProperty("卡号")
	private String cardNumber;

	@ApiModelProperty("卡状态 0-9")
	private Integer status;
}
