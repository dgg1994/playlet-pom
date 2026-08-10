package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("提现试算结果")
public class WithdrawPreviewRespEntity {

	@ApiModelProperty("币种")
	private String assetCode;

	@ApiModelProperty("网络")
	private String network;

	@ApiModelProperty("提现积分")
	private Integer points;

	@ApiModelProperty("汇率：多少积分=1单位币")
	private Integer pointsPerUnit;

	@ApiModelProperty("毛额")
	private BigDecimal grossAmt;

	@ApiModelProperty("手续费")
	private BigDecimal feeAmt;

	@ApiModelProperty("实到")
	private BigDecimal actualAmt;
}
