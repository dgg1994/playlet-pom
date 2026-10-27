package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 三方下单结果：不同场景返回可用字段不同，前端按 tradeType 取用。
 */
@Data
@ApiModel("三方支付下单结果")
public class PayPrepayResult {

	@ApiModelProperty("是否下单成功")
	private boolean success = true;

	@ApiModelProperty("渠道 WECHAT/ALIPAY")
	private String channel;

	@ApiModelProperty("三方交易类型：NATIVE/JSAPI/APP/MWEB/PAGE/WAP")
	private String tradeType;

	@ApiModelProperty("业务订单号")
	private String orderNo;

	@ApiModelProperty("下单金额(元)")
	private BigDecimal payAmount;

	/** 微信 Native：二维码内容，前端转二维码展示 */
	@ApiModelProperty("微信 Native 二维码链接")
	private String codeUrl;

	/** 微信/支付宝预支付交易号 */
	@ApiModelProperty("预支付交易号")
	private String prepayId;

	/** 微信 H5(mweb)/JSAPI/APP 拉起支付所需参数 */
	@ApiModelProperty("拉起支付参数集合")
	private Map<String, Object> payParams;

	/** 支付宝 PC 网站支付：自动提交表单 HTML */
	@ApiModelProperty("支付宝PC支付表单HTML")
	private String payForm;

	/** 支付宝 App 支付：客户端直接透传给 SDK 的订单串 */
	@ApiModelProperty("支付宝App支付订单串")
	private String orderString;

	@ApiModelProperty("失败原因（success=false 时有效）")
	private String errMsg;

	public static PayPrepayResult fail(String channel, String errMsg) {
		PayPrepayResult r = new PayPrepayResult();
		r.setSuccess(false);
		r.setChannel(channel);
		r.setErrMsg(errMsg);
		return r;
	}
}
