package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 作家结算账户（U 卡）展示。
 */
@Data
@ApiModel(value = "作家结算账户", description = "收益页 U 卡提现就绪状态")
public class CreatorSettlementAccountRespEntity {

	@ApiModelProperty("U 卡提现就绪 0未就绪 1可提现")
	private Integer walletWithdrawReady;

	@ApiModelProperty("默认提现卡号（脱敏）")
	private String defaultCardMasked;
}
