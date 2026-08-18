package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("提现首页")
public class WithdrawHomeRespEntity {

	@ApiModelProperty("可用金币（coin_balance - frozen_coin_balance）")
	private Long coinBalance;

	@ApiModelProperty("冻结金币")
	private Long frozenCoinBalance;

	@ApiModelProperty("OnePay 绑定 0未绑定 1已绑定")
	private Integer onepayBindStatus;

	@ApiModelProperty("是否有可用提现资产")
	private Boolean withdrawEnabled;

	@ApiModelProperty("今日全部资产已用提现积分（待处理/打款中/成功）")
	private Integer todayUsedPoints;

	@ApiModelProperty("最近一次收款地址")
	private String lastWalletAddress;

	@ApiModelProperty("可提现资产列表")
	private List<WithdrawAssetItemEntity> assets = new ArrayList<>();

	@Data
	@ApiModel("提现资产项")
	public static class WithdrawAssetItemEntity {
		@ApiModelProperty("币种编码")
		private String assetCode;
		@ApiModelProperty("网络")
		private String network;
		@ApiModelProperty("多少积分=1单位币")
		private Integer pointsPerUnit;
		@ApiModelProperty("手续费")
		private BigDecimal serviceFee;
		@ApiModelProperty("最低提现积分")
		private Integer minWithdrawPoints;
		@ApiModelProperty("单日上限积分，0不限")
		private Integer maxWithdrawPointsDay;
		@ApiModelProperty("该资产今日已用积分")
		private Integer todayUsedPoints;
		@ApiModelProperty("该资产最近收款地址")
		private String lastWalletAddress;
	}
}
