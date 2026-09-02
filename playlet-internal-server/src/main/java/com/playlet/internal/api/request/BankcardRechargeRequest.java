package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 银行卡充值入参（对齐 onetoken RechargeCardEntity / POST /card/topUp）。
 */
@Data
@ApiModel(value = "银行卡充值", description = "POST /wallet/card/topUp")
public class BankcardRechargeRequest {

	@ApiModelProperty(value = "银行卡id", required = true)
	private Long userBankcardId;

	@ApiModelProperty(value = "充值金额（卡到账金额；与 targetAmount 二选一，优先 amount）", required = true)
	private Integer amount;

	@ApiModelProperty(value = "卡到账金额；不传则取 amount")
	private BigDecimal targetAmount;

	@ApiModelProperty(value = "手续费；不传则按卡产品 rechargeFee 费率计算")
	private BigDecimal handlingFees;

	@ApiModelProperty(value = "充值方式：1 钱包余额 2 银行卡；默认 1")
	private Integer payType;

	@ApiModelProperty(value = "支付密码（6 位数字；type 为 true 或未传时必填）", required = true)
	private String payPassword;

	@ApiModelProperty(value = "true 或未传：C 端用户校验支付密码；false：管理/系统调用跳过")
	private Boolean type;

	@ApiModelProperty(value = "商户订单号（幂等；不传则由服务端生成 CR 前缀单号）")
	private String requestOrderId;

	@ApiModelProperty(value = "操作人 id")
	private Integer setUser;

	@ApiModelProperty(value = "操作人名称")
	private String setUserName;
}
