package com.playlet.internal.service.support.pay;

import com.alibaba.fastjson.JSON;
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
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.request.AlipayUserAgreementPageSignRequest;
import com.alipay.api.request.AlipayUserAgreementQueryRequest;
import com.alipay.api.request.AlipayUserAgreementUnsignRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.playlet.internal.api.response.PayContractResult;
import com.playlet.internal.api.response.PayDeductResult;
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

	/** 配置变更后失效缓存的客户端，下次调用按新配置重建 */
	public void invalidate() {
		synchronized (this) {
			alipayClient = null;
		}
	}

	/**
	 * 下单。
	 *
	 * @param scene      支付场景：PC→page、H5/MINI→wap、APP→app
	 * @param subject    订单标题
	 */
	public PayPrepayResult prepay(PaySceneEnums scene, String orderNo, BigDecimal amountYuan,
			String subject, String passbackParams) {
		// 获取支付宝配置
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

	// ------------------------------------------------------ 周期扣款（连续包月）

	/**
	 * 发起签约。返回客户端拉起签约的 form（PC/H5）或 App 拉起串（APP 场景）。
	 * <p>
	 * 用「通用代扣 GENERAL_WITHHOLDING_P」而不是「周期扣款 CYCLE_PAY_AUTH」：
	 * CYCLE_PAY_AUTH 会按 period_rule_params 由支付宝侧自动扣款，与本项目
	 * {@code MemberAutoRenewScheduled} 的服务端主动代扣叠加会造成重复扣款。
	 * 通用代扣只签约、不自动扣，每期由 {@link #agreementPay} 主动发起。
	 *
	 * @param externalNo 商户侧签约号（幂等键，建议用首单号）
	 * @param amountYuan 每期扣款金额，仅用于校验（>0），不下发给三方
	 */
	public PayContractResult agreementSign(PaySceneEnums scene, String externalNo, String productName,
			BigDecimal amountYuan) {
		PayProperties.Alipay ali = payProperties.getAlipay();
		PayContractResult result = new PayContractResult();
		result.setChannel("ALIPAY");
		result.setExternalAgreementNo(externalNo);
		String amountText = amountYuan == null ? null
				: amountYuan.setScale(2, RoundingMode.HALF_UP).toPlainString();
		if (amountText == null || amountYuan.compareTo(BigDecimal.ZERO) <= 0) {
			return PayContractResult.fail("ALIPAY", externalNo, "签约金额必须大于 0");
		}

		JSONObject access = new JSONObject(true);
		access.put("channel", scene == PaySceneEnums.APP ? "ALIPAYAPP" : "QRCODE");

		JSONObject biz = new JSONObject(true);
		biz.put("product_code", ali.getSignProductCode());
		biz.put("personal_product_code", ali.getPersonalProductCode());
		biz.put("external_agreement_no", externalNo);
		biz.put("sign_scene", ali.getSignScene());
		biz.put("prod_name", productName);
		biz.put("access_params", access);
		if (!StringUtils.isEmpty(ali.getSignNotifyUrl())) {
			biz.put("sign_notify_url", ali.getSignNotifyUrl());
		}

		try {
			AlipayUserAgreementPageSignRequest request = new AlipayUserAgreementPageSignRequest();
			request.setBizContent(biz.toJSONString());
			String raw;
			if (scene == PaySceneEnums.APP) {
				raw = client().sdkExecute(request).getBody();
				result.setSignUrl(raw);
			} else {
				raw = client().pageExecute(request).getBody();
				result.setSignForm(raw);
			}
			result.setSuccess(!StringUtils.isEmpty(raw));
			if (!result.isSuccess()) {
				result.setErrMsg("支付宝签约返回为空");
			}
			log.info("alipay agreement sign externalNo={} scene={} productCode={} amount={}",
					externalNo, scene, ali.getSignProductCode(), amountText);
			return result;
		} catch (AlipayApiException e) {
			log.error("alipay agreement sign failed externalNo={}", externalNo, e);
			return PayContractResult.fail("ALIPAY", externalNo, "支付宝签约失败：" + e.getErrMsg());
		}
	}

	/** 查询签约状态；失败返回 null */
	public JSONObject agreementQuery(String externalNo, String agreementNo) {
		PayProperties.Alipay ali = payProperties.getAlipay();
		JSONObject biz = new JSONObject(true);
		biz.put("personal_product_code", ali.getPersonalProductCode());
		if (!StringUtils.isEmpty(agreementNo)) {
			biz.put("agreement_no", agreementNo);
		} else {
			biz.put("external_agreement_no", externalNo);
		}
		try {
			AlipayUserAgreementQueryRequest request = new AlipayUserAgreementQueryRequest();
			request.setBizContent(biz.toJSONString());
			JSONObject json = JSON.parseObject(client().execute(request).getBody())
					.getJSONObject("alipay_user_agreement_query_response");
			return json;
		} catch (Exception e) {
			log.error("alipay agreement query failed externalNo={}", externalNo, e);
			return null;
		}
	}

	/** 解约 */
	public boolean agreementUnsign(String externalNo, String agreementNo) {
		PayProperties.Alipay ali = payProperties.getAlipay();
		JSONObject biz = new JSONObject(true);
		biz.put("personal_product_code", ali.getPersonalProductCode());
		biz.put("sign_scene", ali.getSignScene());
		if (!StringUtils.isEmpty(agreementNo)) {
			biz.put("agreement_no", agreementNo);
		} else {
			biz.put("external_agreement_no", externalNo);
		}
		try {
			AlipayUserAgreementUnsignRequest request = new AlipayUserAgreementUnsignRequest();
			request.setBizContent(biz.toJSONString());
			client().execute(request);
			log.info("alipay agreement unsign ok externalNo={} agreementNo={}", externalNo, agreementNo);
			return true;
		} catch (AlipayApiException e) {
			log.error("alipay agreement unsign failed externalNo={}", externalNo, e);
			return false;
		}
	}

	/**
	 * 按协议号代扣（自动续费每期扣款）。
	 *
	 * @param agreementNo 三方协议号
	 * @param buyerId     支付宝用户 id（buyer_user_id），可选，填了校验更严格
	 */
	public PayDeductResult agreementPay(String agreementNo, String buyerId, String orderNo,
			BigDecimal amountYuan, String subject) {
		String total = amountYuan == null ? null
				: amountYuan.setScale(2, RoundingMode.HALF_UP).toPlainString();
		if (total == null || amountYuan.compareTo(BigDecimal.ZERO) <= 0) {
			return PayDeductResult.fail("ALIPAY", orderNo, "AMOUNT_ERROR", "代扣金额必须大于 0");
		}
		JSONObject agreementParams = new JSONObject(true);
		agreementParams.put("agreement_no", agreementNo);
		if (!StringUtils.isEmpty(buyerId)) {
			agreementParams.put("external_agreement_no", null);
		}

		JSONObject biz = new JSONObject(true);
		biz.put("out_trade_no", orderNo);
		biz.put("total_amount", total);
		biz.put("subject", subject);
		biz.put("product_code", payProperties.getAlipay().getWithholdProductCode());
		biz.put("agreement_params", agreementParams);
		if (!StringUtils.isEmpty(buyerId)) {
			biz.put("buyer_id", buyerId);
		}

		try {
			AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
			request.setNotifyUrl(payProperties.getAlipay().getNotifyUrl());
			request.setBizContent(biz.toJSONString());
			JSONObject json = JSON.parseObject(client().execute(request).getBody())
					.getJSONObject("alipay_trade_create_response");
			PayDeductResult result = new PayDeductResult();
			result.setChannel("ALIPAY");
			result.setOrderNo(orderNo);
			result.setErrCode(json.getString("code"));
			result.setErrMsg(json.getString("sub_msg") == null
					? json.getString("msg") : json.getString("sub_msg"));
			boolean ok = "10000".equals(json.getString("code"));
			result.setSuccess(ok);
			result.setThirdTradeNo(json.getString("trade_no"));
			result.setTradeState(ok ? "SUCCESS" : "FAIL");
			log.info("alipay agreement pay orderNo={} success={} tradeNo={} msg={}",
					orderNo, ok, result.getThirdTradeNo(), result.getErrMsg());
			return result;
		} catch (Exception e) {
			log.error("alipay agreement pay failed orderNo={}", orderNo, e);
			return PayDeductResult.fail("ALIPAY", orderNo, "DEDUCT_FAIL", e.getMessage());
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
