package com.playlet.internal.service.support.pay;

import lombok.Data;

/**
 * 渠道退款结果。
 */
@Data
public class PayRefundResult {

	/** 渠道退款请求是否成功受理/成功（微信为受理结果，支付宝为即时结果） */
	private boolean success;

	/** 渠道退款状态：微信 SUCCESS/PROCESSING/ABNORMAL/CLOSED；支付宝 Y（fundChange=Y） */
	private String status;

	/** 商户退款单号 */
	private String refundNo;

	/** 渠道退款单号 */
	private String thirdRefundNo;

	/** 渠道订单号 */
	private String thirdTradeNo;

	private String channel;

	/** 退款金额(元) */
	private String refundAmount;

	private String errCode;

	private String errMsg;

	public static PayRefundResult fail(String channel, String errCode, String errMsg) {
		PayRefundResult r = new PayRefundResult();
		r.setSuccess(false);
		r.setChannel(channel);
		r.setErrCode(errCode);
		r.setErrMsg(errMsg);
		return r;
	}
}
