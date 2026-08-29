package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * USDT 链上充值回调。
 */
@Data
@ApiModel(value = "USDT充值回调", description = "网关 POST /wallet/topinUsdtNotify")
public class UsdtTopinNotifyRequest {

	@ApiModelProperty("钱包三方 uid")
	private String uid;

	@ApiModelProperty("邮箱")
	private String email;

	@ApiModelProperty("地址类型")
	private String address;

	@ApiModelProperty("类型")
	private String type;

	@ApiModelProperty("订单号")
	private String order_no;

	@ApiModelProperty("币种")
	private String coin;

	@ApiModelProperty("时间")
	private String time;

	@ApiModelProperty("到账金额")
	private String amount;

	@ApiModelProperty("转出地址")
	private String outaddress;

	@ApiModelProperty("转入地址")
	private String inaddress;

	@ApiModelProperty("链上交易 hash")
	private String hash;

	@ApiModelProperty("签名")
	private String sign;
}
