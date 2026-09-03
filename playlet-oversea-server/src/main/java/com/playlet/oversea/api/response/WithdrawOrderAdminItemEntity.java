package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端提现记录列表行（对齐财务页表格列）。
 */
@Data
@ApiModel(value = "提现订单管理列表项", description = "流水ID/用户名称/提现金币/折合货币/支付方式/支付账户")
public class WithdrawOrderAdminItemEntity {

	@ApiModelProperty("主键（操作/勾选用）")
	private Long id;

	@ApiModelProperty("流水ID")
	private String orderNo;

	@ApiModelProperty("用户uid或作家id")
	private Integer uid;

	@ApiModelProperty("用户名称 / 作者名称")
	private String userName;

	@ApiModelProperty("提现金币")
	private Integer withdrawCoin;

	@ApiModelProperty("折合货币")
	private BigDecimal currencyAmt;

	@ApiModelProperty("支付方式")
	private String payMethod;

	@ApiModelProperty("支付账户")
	private String payAccount;

	@ApiModelProperty("资产编码（内部）")
	private String assetCode;

	@ApiModelProperty("打款网关（内部）")
	private String gateway;

	@ApiModelProperty("网络（内部）")
	private String network;

	@ApiModelProperty("状态码：0待处理 1打款中 2成功 3失败 4已退回")
	private Integer status;

	@ApiModelProperty("状态文案")
	private String statusLabel;

	@ApiModelProperty("申请时间")
	private Date setTime;
}
