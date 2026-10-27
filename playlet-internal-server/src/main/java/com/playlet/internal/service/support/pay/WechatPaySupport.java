package com.playlet.internal.service.support.pay;

import com.playlet.internal.api.response.PayPrepayResult;
import com.playlet.internal.config.PayProperties;
import com.playlet.internal.enums.PaySceneEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.StringUtils;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.app.AppServiceExtension;
import com.wechat.pay.java.service.payments.app.model.PrepayRequest;
import com.wechat.pay.java.service.payments.app.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付 APIv3：Native/JSAPI/App/H5 下单、退款、查单、回调报文解析。
 *
 * <p>SDK 不会自动填充 appid/mchid，需由本类从配置写入请求体；金额为分。
 */
@Slf4j
@Component
public class WechatPaySupport {

	@Autowired
	private PayProperties payProperties;

	private volatile RSAAutoCertificateConfig config;
	private volatile NativePayService nativePayService;
	private volatile JsapiServiceExtension jsapiServiceExtension;
	private volatile AppServiceExtension appServiceExtension;
	private volatile H5Service h5Service;
	private volatile RefundService refundService;
	private volatile NotificationParser notificationParser;

	/** 该渠道是否已启用（总开关 + 渠道开关） */
	public boolean enabled() {
		PayProperties.Wechat wx = payProperties.getWechat();
		return Boolean.TRUE.equals(payProperties.getEnabled())
				&& wx != null && Boolean.TRUE.equals(wx.getEnabled());
	}

	/**
	 * 下单。
	 *
	 * @param scene       支付场景
	 * @param orderNo     商户订单号
	 * @param amountYuan  金额(元)
	 * @param description 商品描述
	 * @param openId      JSAPI 场景必填
	 */
	public PayPrepayResult prepay(PaySceneEnums scene, String orderNo, BigDecimal amountYuan,
			String description, String openId) {
		PayProperties.Wechat wx = payProperties.getWechat();
		RSAAutoCertificateConfig cfg = config();
		long total = toCents(amountYuan);
		PayPrepayResult result = new PayPrepayResult();
		result.setChannel("WECHAT");
		result.setOrderNo(orderNo);
		result.setPayAmount(amountYuan);

		PaySceneEnums target = scene == null ? PaySceneEnums.PC : scene;
		if (target == PaySceneEnums.MINI && StringUtils.isEmpty(openId)) {
			// 无 openid 时降级为 Native 扫码，保证仍可支付
			target = PaySceneEnums.PC;
		}

		if (target == PaySceneEnums.H5) {
			com.wechat.pay.java.service.payments.h5.model.PrepayRequest req =
					new com.wechat.pay.java.service.payments.h5.model.PrepayRequest();
			fillBase(req, wx, orderNo, total, description, null);
			com.wechat.pay.java.service.payments.h5.model.PrepayResponse resp = h5Service().prepay(req);
			result.setTradeType("MWEB");
			Map<String, Object> params = new HashMap<>(4);
			params.put("mwebUrl", resp.getH5Url());
			result.setPayParams(params);
			result.setPrepayId(null);
		} else if (target == PaySceneEnums.APP) {
			PrepayRequest req = new PrepayRequest();
			fillBase(req, wx, orderNo, total, description, null);
			PrepayWithRequestPaymentResponse resp = appServiceExtension().prepayWithRequestPayment(req);
			result.setTradeType("APP");
			result.setPayParams(toJsapiParams(resp));
		} else if (target == PaySceneEnums.MINI) {
			com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest req =
					new com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest();
			Payer payer = new Payer();
			payer.setOpenid(openId);
			req.setPayer(payer);
			fillBase(req, wx, orderNo, total, description, null);
			com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse resp =
					jsapiServiceExtension().prepayWithRequestPayment(req);
			result.setTradeType("JSAPI");
			result.setPayParams(toJsapiParams(resp));
		} else {
			com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest req =
					new com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest();
			fillBase(req, wx, orderNo, total, description, null);
			com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse resp =
					nativePayService().prepay(req);
			result.setTradeType("NATIVE");
			result.setCodeUrl(resp.getCodeUrl());
		}
		log.info("wechat prepay orderNo={} tradeType={} total={}", orderNo, result.getTradeType(), total);
		return result;
	}

	/**
	 * 申请退款（原路退回）。
	 *
	 * @param refundNo        商户退款单号，同一单号重复请求即幂等
	 * @param payAmountYuan   原订单金额(元)
	 * @param refundAmountYuan 退款金额(元)
	 */
	public PayRefundResult refund(String orderNo, String refundNo,
			BigDecimal payAmountYuan, BigDecimal refundAmountYuan, String reason) {
		PayProperties.Wechat wx = payProperties.getWechat();
		RSAAutoCertificateConfig cfg = config();
		CreateRequest req = new CreateRequest();
		req.setOutTradeNo(orderNo);
		req.setOutRefundNo(refundNo);
		req.setReason(reason);
		req.setNotifyUrl(wx.getRefundNotifyUrl());
		AmountReq amount = new AmountReq();
		amount.setTotal(toCents(payAmountYuan));
		amount.setRefund(toCents(refundAmountYuan));
		amount.setCurrency("CNY");
		req.setAmount(amount);

		Refund refund = refundService().create(req);
		PayRefundResult result = new PayRefundResult();
		result.setChannel("WECHAT");
		result.setRefundNo(refund.getOutRefundNo());
		result.setThirdRefundNo(refund.getRefundId());
		result.setThirdTradeNo(refund.getTransactionId());
		result.setStatus(refund.getStatus() == null ? null : refund.getStatus().name());
		// SUCCESS/PROCESSING 均视为受理成功；ABNORMAL/CLOSED 需人工介入
		result.setSuccess(refund.getStatus() != null
				&& !"ABNORMAL".equals(refund.getStatus().name())
				&& !"CLOSED".equals(refund.getStatus().name()));
		log.info("wechat refund orderNo={} refundNo={} status={} success={}",
				orderNo, refundNo, result.getStatus(), result.isSuccess());
		return result;
	}

	/** 退款查询（对账/补偿可用） */
	public PayRefundResult queryRefund(String refundNo) {
		QueryByOutRefundNoRequest req = new QueryByOutRefundNoRequest();
		req.setOutRefundNo(refundNo);
		Refund refund = refundService().queryByOutRefundNo(req);
		PayRefundResult result = new PayRefundResult();
		result.setChannel("WECHAT");
		result.setRefundNo(refund.getOutRefundNo());
		result.setThirdRefundNo(refund.getRefundId());
		result.setThirdTradeNo(refund.getTransactionId());
		result.setStatus(refund.getStatus() == null ? null : refund.getStatus().name());
		result.setSuccess("SUCCESS".equals(result.getStatus()));
		return result;
	}

	/** 按商户订单号查单 */
	public PayOrderResult queryOrder(String orderNo) {
		com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest req =
				new com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest();
		req.setOutTradeNo(orderNo);
		req.setMchid(payProperties.getWechat().getMchId());
		Transaction tx = nativePayService().queryOrderByOutTradeNo(req);
		return toOrderResult(tx);
	}

	/** 按微信支付单号查单 */
	public PayOrderResult queryOrderByTradeNo(String transactionId) {
		com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByIdRequest req =
				new com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByIdRequest();
		req.setTransactionId(transactionId);
		req.setMchid(payProperties.getWechat().getMchId());
		Transaction tx = nativePayService().queryOrderById(req);
		return toOrderResult(tx);
	}

	/** 关闭未支付订单 */
	public void closeOrder(String orderNo) {
		com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest req =
				new com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest();
		req.setOutTradeNo(orderNo);
		req.setMchid(payProperties.getWechat().getMchId());
		nativePayService().closeOrder(req);
	}

	/** 解析支付结果通知；验签/解密失败会抛异常 */
	public Transaction parsePayNotify(String serial, String timestamp, String nonce,
			String signature, String body) {
		RequestParam param = new RequestParam.Builder()
				.serialNumber(serial)
				.timestamp(timestamp)
				.nonce(nonce)
				.signature(signature)
				.body(body)
				.build();
		return notificationParser().parse(param, Transaction.class);
	}

	/** 解析退款结果通知；验签/解密失败会抛异常 */
	public RefundNotification parseRefundNotify(String serial, String timestamp, String nonce,
			String signature, String body) {
		RequestParam param = new RequestParam.Builder()
				.serialNumber(serial)
				.timestamp(timestamp)
				.nonce(nonce)
				.signature(signature)
				.body(body)
				.build();
		return notificationParser().parse(param, RefundNotification.class);
	}

	private PayOrderResult toOrderResult(Transaction tx) {
		PayOrderResult result = new PayOrderResult();
		result.setSuccess(true);
		result.setOrderNo(tx.getOutTradeNo());
		result.setThirdTradeNo(tx.getTransactionId());
		result.setTradeState(tx.getTradeState() == null ? null : tx.getTradeState().name());
		result.setPaid("SUCCESS".equals(result.getTradeState()));
		if (tx.getAmount() != null && tx.getAmount().getTotal() != null) {
			result.setPayAmount(BigDecimal.valueOf(tx.getAmount().getTotal())
					.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
		}
		return result;
	}

	/** JSAPI/小程序：App 端调起 JS 支付所需签名参数 */
	private Map<String, Object> toJsapiParams(
			com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse resp) {
		Map<String, Object> params = new HashMap<>(8);
		params.put("appId", resp.getAppId());
		params.put("timeStamp", resp.getTimeStamp());
		params.put("nonceStr", resp.getNonceStr());
		params.put("package", resp.getPackageVal());
		params.put("signType", resp.getSignType());
		params.put("paySign", resp.getPaySign());
		return params;
	}

	/** APP 支付：客户端调起 SDK 所需签名参数 */
	private Map<String, Object> toJsapiParams(PrepayWithRequestPaymentResponse resp) {
		Map<String, Object> params = new HashMap<>(8);
		params.put("appid", resp.getAppid());
		params.put("partnerid", resp.getPartnerId());
		params.put("prepayid", resp.getPrepayId());
		params.put("package", resp.getPackageVal());
		params.put("noncestr", resp.getNonceStr());
		params.put("timestamp", resp.getTimestamp());
		params.put("sign", resp.getSign());
		return params;
	}

private void fillBase(com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest req,
			PayProperties.Wechat wx, String orderNo, long total, String description, String attach) {
		req.setAppid(wx.getAppId());
		req.setMchid(wx.getMchId());
		req.setDescription(description);
		req.setOutTradeNo(orderNo);
		req.setNotifyUrl(wx.getNotifyUrl());
		req.setAttach(attach);
		com.wechat.pay.java.service.payments.nativepay.model.Amount amount =
				new com.wechat.pay.java.service.payments.nativepay.model.Amount();
		amount.setTotal((int) total);
		amount.setCurrency("CNY");
		req.setAmount(amount);
	}

	private void fillBase(PrepayRequest req, PayProperties.Wechat wx, String orderNo,
			long total, String description, String attach) {
		req.setAppid(wx.getAppId());
		req.setMchid(wx.getMchId());
		req.setDescription(description);
		req.setOutTradeNo(orderNo);
		req.setNotifyUrl(wx.getNotifyUrl());
		req.setAttach(attach);
		com.wechat.pay.java.service.payments.app.model.Amount amount =
				new com.wechat.pay.java.service.payments.app.model.Amount();
		amount.setTotal((int) total);
		amount.setCurrency("CNY");
		req.setAmount(amount);
	}

	private void fillBase(com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest req,
			PayProperties.Wechat wx, String orderNo, long total, String description, String attach) {
		req.setAppid(wx.getAppId());
		req.setMchid(wx.getMchId());
		req.setDescription(description);
		req.setOutTradeNo(orderNo);
		req.setNotifyUrl(wx.getNotifyUrl());
		req.setAttach(attach);
		com.wechat.pay.java.service.payments.jsapi.model.Amount amount =
				new com.wechat.pay.java.service.payments.jsapi.model.Amount();
		amount.setTotal((int) total);
		amount.setCurrency("CNY");
		req.setAmount(amount);
	}

	private void fillBase(com.wechat.pay.java.service.payments.h5.model.PrepayRequest req,
			PayProperties.Wechat wx, String orderNo, long total, String description, String attach) {
		req.setAppid(wx.getAppId());
		req.setMchid(wx.getMchId());
		req.setDescription(description);
		req.setOutTradeNo(orderNo);
		req.setNotifyUrl(wx.getNotifyUrl());
		req.setAttach(attach);
		com.wechat.pay.java.service.payments.h5.model.Amount amount =
				new com.wechat.pay.java.service.payments.h5.model.Amount();
		amount.setTotal((int) total);
		amount.setCurrency("CNY");
		req.setAmount(amount);
	}

	/** 元转分；最少 1 分 */
	private long toCents(BigDecimal amountYuan) {
		if (amountYuan == null || amountYuan.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BaseException("支付金额必须大于 0");
		}
		return amountYuan.multiply(BigDecimal.valueOf(100))
				.setScale(0, RoundingMode.HALF_UP).longValue();
	}

	private RSAAutoCertificateConfig config() {
		if (config == null) {
			synchronized (this) {
				if (config == null) {
					PayProperties.Wechat wx = payProperties.getWechat();
					if (wx == null || StringUtils.isEmpty(wx.getMchId())
							|| StringUtils.isEmpty(wx.getMchSerialNo())
							|| StringUtils.isEmpty(wx.getApiV3Key())
							|| (StringUtils.isEmpty(wx.getPrivateKeyPath()) && StringUtils.isEmpty(wx.getPrivateKey()))) {
						throw new BaseException("微信支付配置不完整");
					}
					RSAAutoCertificateConfig.Builder builder = new RSAAutoCertificateConfig.Builder()
							.merchantId(wx.getMchId())
							.merchantSerialNumber(wx.getMchSerialNo())
							.apiV3Key(wx.getApiV3Key());
					if (StringUtils.isEmpty(wx.getPrivateKeyPath())) {
						builder.privateKey(wx.getPrivateKey());
					} else {
						builder.privateKeyFromPath(wx.getPrivateKeyPath());
					}
					config = builder.build();
					log.info("wechat pay client initialized mchId={} appId={}", wx.getMchId(), wx.getAppId());
				}
			}
		}
		return config;
	}

	private NativePayService nativePayService() {
		if (nativePayService == null) {
			synchronized (this) {
				if (nativePayService == null) {
					nativePayService = new NativePayService.Builder().config(config()).build();
				}
			}
		}
		return nativePayService;
	}

	private JsapiServiceExtension jsapiServiceExtension() {
		if (jsapiServiceExtension == null) {
			synchronized (this) {
				if (jsapiServiceExtension == null) {
					jsapiServiceExtension = new JsapiServiceExtension.Builder().config(config()).build();
				}
			}
		}
		return jsapiServiceExtension;
	}

	private AppServiceExtension appServiceExtension() {
		if (appServiceExtension == null) {
			synchronized (this) {
				if (appServiceExtension == null) {
					appServiceExtension = new AppServiceExtension.Builder().config(config()).build();
				}
			}
		}
		return appServiceExtension;
	}

	private H5Service h5Service() {
		if (h5Service == null) {
			synchronized (this) {
				if (h5Service == null) {
					h5Service = new H5Service.Builder().config(config()).build();
				}
			}
		}
		return h5Service;
	}

	private RefundService refundService() {
		if (refundService == null) {
			synchronized (this) {
				if (refundService == null) {
					refundService = new RefundService.Builder().config(config()).build();
				}
			}
		}
		return refundService;
	}

	private NotificationParser notificationParser() {
		if (notificationParser == null) {
			synchronized (this) {
				if (notificationParser == null) {
					notificationParser = new NotificationParser(config());
				}
			}
		}
		return notificationParser;
	}
}
