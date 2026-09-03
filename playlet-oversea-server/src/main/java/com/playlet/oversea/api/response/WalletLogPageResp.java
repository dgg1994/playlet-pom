package com.playlet.oversea.api.response;

import com.github.pagehelper.PageInfo;
import com.playlet.oversea.entity.wallet.WalletLogEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 钱包账变分页 + 月度收支汇总。
 */
@Data
@ApiModel(value = "钱包账变分页", description = "列表与当月收支统计")
public class WalletLogPageResp {

	@ApiModelProperty(name = "pageInfo", value = "分页数据")
	private PageInfo<WalletLogEntity> pageInfo;

	@ApiModelProperty(name = "totalIncome", value = "当月收入合计")
	private BigDecimal totalIncome;

	@ApiModelProperty(name = "totalExpenses", value = "当月支出合计")
	private BigDecimal totalExpenses;
}
