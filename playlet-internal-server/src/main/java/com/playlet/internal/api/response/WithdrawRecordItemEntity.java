package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("提现记录项")
public class WithdrawRecordItemEntity {

	@ApiModelProperty("订单ID")
	private Long id;

	@ApiModelProperty("业务单号")
	private String orderNo;

	@ApiModelProperty("币种")
	private String assetCode;

	@ApiModelProperty("网络")
	private String network;

	@ApiModelProperty("扣减积分（展示为负数）")
	private Integer pointsAmt;

	@ApiModelProperty("实到金额")
	private BigDecimal actualAmt;

	@ApiModelProperty("收款地址（脱敏）")
	private String walletAddressMasked;

	@ApiModelProperty("状态码")
	private Integer status;

	@ApiModelProperty("状态文案")
	private String statusLabel;

	@ApiModelProperty("链上哈希")
	private String txHash;

	@ApiModelProperty("失败原因")
	private String failReason;

	@ApiModelProperty("创建时间")
	private Date setTime;
}
