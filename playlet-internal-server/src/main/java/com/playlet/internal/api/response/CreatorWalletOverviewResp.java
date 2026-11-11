package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 我的钱包 - 顶部资金概览。
 * <p>金额单位统一为「金币」，由前端格式化为 ¥。</p>
 */
@Data
@ApiModel(value = "钱包资金概览")
public class CreatorWalletOverviewResp {

	@ApiModelProperty(value = "账户余额")
	private Long accountBalance;

	@ApiModelProperty(value = "可提现金额（余额 - 冻结）")
	private Long availableWithdraw;

	@ApiModelProperty(value = "待结算金额")
	private Long pendingSettle;

	@ApiModelProperty(value = "累计总收益")
	private Long totalIncome;

	@ApiModelProperty(value = "单笔最低提现金额")
	private Long minWithdrawAmount;
}
