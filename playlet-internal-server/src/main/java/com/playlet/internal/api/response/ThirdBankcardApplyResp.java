package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 申请银行卡结果。
 */
@Data
@ApiModel(value = "申请银行卡结果", description = "POST /api/bankcard/apply data")
public class ThirdBankcardApplyResp {

	@ApiModelProperty("三方订单号")
	private String orderNo;

	@ApiModelProperty("卡号（申请时可能为空）")
	private String cardNo;

	@ApiModelProperty("对方银行卡id")
	private Long userBankcardId;
}
