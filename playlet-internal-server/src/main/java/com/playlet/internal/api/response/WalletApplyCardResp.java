package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 申请开卡结果。
 */
@Data
@ApiModel(value = "申请开卡结果", description = "本地申请单 + 三方卡 id")
public class WalletApplyCardResp {

	@ApiModelProperty("本地申请单 id")
	private Long applyId;

	@ApiModelProperty("三方订单号")
	private String orderNo;

	@ApiModelProperty("对方 userBankcardId")
	private Long userBankcardId;

	@ApiModelProperty("卡号（申请时可能为空）")
	private String cardNo;

	@ApiModelProperty("本地 wallet_bankcard.id（已落库时）")
	private Long walletBankcardId;
}
