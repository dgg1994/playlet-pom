package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * USDT 链上充值回调（字段对齐 onetoken UsdtTopinNotifyEntity）。
 */
@Data
@ApiModel(value = "USDT充值回调", description = "网关 POST /wallet/topinUsdtNotify")
public class UsdtTopinNotifyRequest {

	@ApiModelProperty("接收/发送，in=转入")
	private String type;

	@ApiModelProperty("区块")
	private String block;

	@ApiModelProperty("钱包三方 uid")
	private String uid;

	@ApiModelProperty("用户邮箱")
	private String email;

	@ApiModelProperty("订单号")
	private String orderNo;

	@ApiModelProperty("网络类型")
	private String chain;

	@ApiModelProperty("代币")
	private String coin;

	@ApiModelProperty("到账金额")
	private String amount;

	@ApiModelProperty("交易金额原始值")
	private String amount_raw;

	@ApiModelProperty("交易时间")
	private String time;

	@ApiModelProperty("转出地址")
	private String outaddress;

	@ApiModelProperty("转入地址")
	private String inaddress;

	@ApiModelProperty("签名")
	private String sign;

	@ApiModelProperty("链上交易 hash")
	private String hash;

	@ApiModelProperty("调用时传的订单号")
	private String requestOrderNum;

	@ApiModelProperty("处理状态")
	private Integer stateCode;
}
