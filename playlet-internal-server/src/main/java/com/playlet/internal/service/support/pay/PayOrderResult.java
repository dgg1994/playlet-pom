package com.playlet.internal.service.support.pay;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 渠道订单查询结果。
 */
@Data
public class PayOrderResult {

	/** 查询是否成功（不代表已支付） */
	private boolean success;

	/** 是否已支付成功 */
	private boolean paid;

	/** 渠道返回的交易状态 */
	private String tradeState;

	private String orderNo;

	/** 渠道交易号 */
	private String thirdTradeNo;

	/** 渠道实付金额(元) */
	private BigDecimal payAmount;

	private String errMsg;

	public static PayOrderResult fail(String errMsg) {
		PayOrderResult r = new PayOrderResult();
		r.setSuccess(false);
		r.setErrMsg(errMsg);
		return r;
	}
}
