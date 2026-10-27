package com.playlet.internal.service;

import com.playlet.internal.api.request.PayPrepayReqEntity;
import com.playlet.internal.api.request.PayQueryReqEntity;
import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * C 端支付：网关 /entrance/api/pay/**
 *
 * <p>回调地址三方配置示例：https://{域名}/entrance/api/pay/wechat/pay/notify
 */
@RequestMapping("/api/pay")
@Api(value = "支付", tags = "支付")
public interface PayApiService {

	@PostMapping("/prepay")
	@ApiOperation(value = "重新获取支付参数", notes = "需登录；仅待支付现金订单，返回拉起支付所需参数")
	ResponseBase prepay(HttpServletRequest request, @RequestBody PayPrepayReqEntity req);

	@PostMapping("/query")
	@ApiOperation(value = "查询付款状态", notes = "需登录；查渠道订单，已支付则补单")
	ResponseBase query(HttpServletRequest request, @RequestBody PayQueryReqEntity req);

	@PostMapping("/wechat/pay/notify")
	@ApiOperation(value = "微信支付结果回调", notes = "无需登录；平台证书验签后才落账")
	void wechatPayNotify(HttpServletRequest request, HttpServletResponse response);

	@PostMapping("/wechat/refund/notify")
	@ApiOperation(value = "微信退款结果回调", notes = "无需登录；退款成功后补齐本地退款状态")
	void wechatRefundNotify(HttpServletRequest request, HttpServletResponse response);

	@PostMapping("/alipay/pay/notify")
	@ApiOperation(value = "支付宝支付结果回调", notes = "无需登录；验签通过后落账，返回 success/failure")
	void alipayPayNotify(HttpServletRequest request, HttpServletResponse response);
}
