package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 提现申请入参。
 */
@Data
@ApiModel(value = "提现申请入参")
public class CreatorWalletApplyQuery {

	@NotNull(message = "提现金额不能为空")
	@ApiModelProperty(value = "提现金额（金币），必填", required = true)
	private Long amount;

	@ApiModelProperty("收款银行/渠道，空则用「结算银行卡」")
	private String bankName;

	@ApiModelProperty("银行卡尾号（选填，仅展示）")
	private String bankCardTail;
}
