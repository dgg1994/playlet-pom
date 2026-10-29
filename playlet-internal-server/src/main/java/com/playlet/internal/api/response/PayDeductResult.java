package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 代扣（自动续费扣款）结果。
 */
@Data
@ApiModel("代扣结果")
public class PayDeductResult {

	@ApiModelProperty("渠道 WECHAT/ALIPAY")
	private String channel;

	@ApiModelProperty("商户订单号")
	private String orderNo;

	@ApiModelProperty("是否扣款成功")
	private boolean success;

	@ApiModelProperty("三方交易号")
	private String thirdTradeNo;

	@ApiModelProperty("三方交易状态")
	private String tradeState;

	@ApiModelProperty("错误码")
	private String errCode;

	@ApiModelProperty("错误描述")
	private String errMsg;

	public static PayDeductResult fail(String channel, String orderNo, String errCode, String errMsg) {
		PayDeductResult result = new PayDeductResult();
		result.setChannel(channel);
		result.setOrderNo(orderNo);
		result.setSuccess(false);
		result.setErrCode(errCode);
		result.setErrMsg(errMsg);
		return result;
	}
}
