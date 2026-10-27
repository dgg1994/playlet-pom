package com.playlet.internal.service.support.pay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.playlet.internal.api.response.PayPrepayResult;
import com.playlet.internal.config.PayProperties;
import com.playlet.internal.enums.PaySceneEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝：PC 网站支付 / 手机网站支付 / App 支付、退款、查单、异步通知验签。
 */
@Slf4j
@Component
public class AlipaySupport {

	@Autowired
	private PayProperties payProperties;

	private volatile AlipayClient alipayClient;

	public boolean enabled() {
		PayProperties.Alipay ali = payProperties.getAlipay();
		return Boolean.TRUE.equals(payProperties.getEnabled())
				&& ali != null && Boolean.TRUE.equals(ali.getEnabled());
	}

	/**
	 * 下单。
	 *
	 * @param scene      支付场景：PC→page、H5/MINI→wap、APP→app
	 * @param subject    订单标题
	 */
	public PayPrepayResult prepay(PaySceneEnums scene, String orderNo, BigDecimal amountYuan,
			String subject, String passbackParams) {
		PayProperties.Alipay ali = payProperties.getAlipay();
		PayPrepayResult result = new PayPrepayResult();
		result.setChannel("ALIPAY");
		result.setOrderNo(orderNo);
		result.setPayAmount(amountYuan);

		String totalAmount = amountYuan == null ? null
				: amountYuan.setScale(2, RoundingMode.HALF_UP).toPlainString();
		if (totalAmount == null || amountYuan.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BaseException("支付金额必须大于 0");
		}
		JSONObject biz = new JSONObject(true);
		biz.put("out_trade_no", orderNo);
		biz.put("total_amount", totalAmount);
		biz.put("subject", subject);
		biz.put("timeout_express", ali.getTimeoutExpress());
		if (!StringUtils.isEmpty(passbackParams)) {
			biz.put("passback_params", passbackParams);
		}

		PaySceneEnums target = scene == null ? PaySceneEnums.PC : scene;
		try {
			if (target == PaySceneEnums.APP) {
				biz.put("product_code", "QUICK_MSECURITY_PAY");
				AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
				request.setNotifyUrl(ali.getNotifyUrl());
				request.setBizContent(biz.toJSONString());
				AlipayTradeAppPayResponse response = client().sdkExecute(request);
				result.setTradeType("APP");
				result.setOrderString(response.getBody());
				checkAlipayResponse(response.getBody() != null, response.getSubCode(), response.getSubMsg());
			} else if (target == PaySceneEnums.PC) {
				biz.put("product_code", "FAST_INSTANT_TRADE_PAY");
				AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
				request.setNotifyUrl(ali.getNotifyUrl());
				request.setReturnUrl(ali.getReturnUrl());
				request.setBizContent(biz.toJSONString());
				AlipayTradePagePayResponse response = client().pageExecute(request);
				result.setTradeType("PAGE");
				result.setPayForm(response.getBody());
				checkAlipayResponse(response.getBody() != null, response.getSubCode(), response.getSubMsg());
			} else {
				biz.put("product_code", "QUICK_WAP_WAY");
				AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
				request.setNotifyUrl(ali.getNotifyUrl());
				request.setReturnUrl(ali.getReturnUrl());
				request.setBizContent(biz.toJSONString());
				AlipayTradeWapPayResponse response = client().pageExecute(request);
				result.setTradeType("WAP");
				result.setPayForm(response.getBody());
				checkAlipayResponse(response.getBody() != null, response.getSubCode(), response.getSubMsg());
			}
		} catch (AlipayApiException e) {
			log.error("alipay prepay failed orderNo={}", orderNo, e);
			throw new BaseException("支付宝下单失败：" + e.getErrMsg(), e);
		}
		log.info("alipay prepay orderNo={} tradeType={} amount={}", orderNo, result.getTradeType(), totalAmount);
		return result;
	}

	/**
	 * 退款（原路退回）。
	 *
	 * @param refundNo 同一 out_request_no 重复请求即幂等（同一笔退款多次退，须用不同退款单号）
	 */
	public PayRefundResult refund(String orderNo, String thirdTradeNo, String refundNo,
			BigDecimal refundAmountYuan, String reason) {
		JSONObject biz = new JSONObject(true);
		if (StringUtils.isEmpty(thirdTradeNo)) {
			biz.put("out_trade_no", orderNo);
		} else {
			biz.put("trade_no", thirdTradeNo);
		}
		biz.put("out_request_no", refundNo);
		biz.put("refund_amount", refundAmountYuan.setScale(2, RoundingMode.HALF_UP).toPlainString());
		biz.put("refund_reason", reason);

		try {
			AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
			request.setBizContent(biz.toJSONString());
			AlipayTradeRefundResponse response = client().execute(request);
			PayRefundResult result = new PayRefundResult();
			result.setChannel("ALIPAY");
			result.setRefundNo(refundNo);
			result.setThirdRefundNo(response.getTradeNo());
			result.setThirdTradeNo(response.getTradeNo());
			result.setRefundAmount(response.getRefundFee());
			if (!response.isSuccess()) {
				result.setSuccess(false);
				result.setErrCode(response.getSubCode());
				result.setErrMsg(response.getSubMsg());
			} else {
				// 支付宝退款同步返回即最终结果：fundChange=Y 表示金额已退到买家
				result.setSuccess(true);
				result.setStatus("Y".equals(response.getFundChange()) ? "SUCCESS" : "PROCESSING");
			}
			log.info("alipay refund orderNo={} refundNo={} success={} fundChange={} subMsg={}",
					orderNo, refundNo, result.isSuccess(), response.getFundChange(), response.getSubMsg());
			return result;
		} catch (AlipayApiException e) {
			log.error("alipay refund failed orderNo={} refundNo={}", orderNo, refundNo, e);
			throw new BaseException("支付宝退款失败：" + e.getErrMsg(), e);
		}
	}

	/** 查单：tradeNo 为空时按商户订单号查 */
	public PayOrderResult queryOrder(String orderNo, String thirdTradeNo) {
		JSONObject biz = new JSONObject(true);
		if (StringUtils.isEmpty(thirdTradeNo)) {
			biz.put("out_trade_no", orderNo);
		} else {
			biz.put("trade_no", thirdTradeNo);
		}
		try {
			AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
			request.setBizContent(biz.toJSONString());
			AlipayTradeQueryResponse response = client().execute(request);
			PayOrderResult result = new PayOrderResult();
			if (!response.isSuccess()) {
				return PayOrderResult.fail(response.getSubMsg());
			}
			result.setSuccess(true);
			result.setOrderNo(response.getOutTradeNo());
			result.setThirdTradeNo(response.getTradeNo());
			result.setTradeState(response.getTradeStatus());
			result.setPaid("TRADE_SUCCESS".equals(response.getTradeStatus())
					|| "TRADE_FINISHED".equals(response.getTradeStatus()));
			if (response.getTotalAmount() != null) {
				result.setPayAmount(new BigDecimal(response.getTotalAmount()));
			}
			return result;
		} catch (AlipayApiException e) {
			log.error("alipay query order failed orderNo={}", orderNo, e);
			throw new BaseException("支付宝查单失败：" + e.getErrMsg(), e);
		}
	}

	/** 关闭未支付订单 */
	public void closeOrder(String orderNo) {
		JSONObject biz = new JSONObject(true);
		biz.put("out_trade_no", orderNo);
		try {
			AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
			request.setBizContent(biz.toJSONString());
			client().execute(request);
		} catch (AlipayApiException e) {
			log.warn("alipay close order failed orderNo={} msg={}", orderNo, e.getErrMsg());
		}
	}

	/** 异步通知验签 */
	public boolean verifyNotify(Map<String, String> params) {
		PayProperties.Alipay ali = payProperties.getAlipay();
		if (StringUtils.isEmpty(ali.getAlipayPublicKey())) {
			throw new BaseException("支付宝公钥未配置");
		}
		try {
			return AlipaySignature.rsaCheckV1(params, ali.getAlipayPublicKey(),
					ali.getCharset(), ali.getSignType());
		} catch (AlipayApiException e) {
			log.error("alipay notify verify error", e);
			return false;
		}
	}

	/** AlipayClient 懒加载 */
	public AlipayClient client() {
		if (alipayClient == null) {
			synchronized (this) {
				if (alipayClient == null) {
					PayProperties.Alipay ali = payProperties.getAlipay();
					if (ali == null || StringUtils.isEmpty(ali.getAppId())
							|| StringUtils.isEmpty(ali.getPrivateKey())
							|| StringUtils.isEmpty(ali.getAlipayPublicKey())
							|| StringUtils.isEmpty(ali.getGatewayUrl())) {
						throw new BaseException("支付宝配置不完整");
					}
					alipayClient = new DefaultAlipayClient(ali.getGatewayUrl(), ali.getAppId(),
							ali.getPrivateKey(), ali.getFormat(), ali.getCharset(),
							ali.getAlipayPublicKey(), ali.getSignType());
					log.info("alipay client initialized appId={} gateway={}", ali.getAppId(), ali.getGatewayUrl());
				}
			}
		}
		return alipayClient;
	}

	private void checkAlipayResponse(boolean hasBody, String subCode, String subMsg) {
		if (!hasBody && StringUtils.isEmpty(subCode)) {
			throw new BaseException("支付宝下单失败：" + subMsg);
		}
		if (!StringUtils.isEmpty(subCode)) {
			throw new BaseException("支付宝下单失败：" + subCode + " " + subMsg);
		}
	}

	/** 通知参数包一层：保留原始 map 便于排查（未使用则忽略） */
	public Map<String, String> wrapParams(Map<String, String[]> rawParams) {
		Map<String, String> params = new HashMap<>(rawParams.size());
		for (Map.Entry<String, String[]> entry : rawParams.entrySet()) {
			String[] values = entry.getValue();
			params.put(entry.getKey(), values == null || values.length == 0 ? null : values[0]);
		}
		return params;
	}
}
