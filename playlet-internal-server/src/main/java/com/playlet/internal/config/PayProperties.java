package com.playlet.internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 三方支付配置（微信支付 APIv3 / 支付宝）。
 *
 * <p>yml 前缀 pay：总开关 pay.enabled 关闭时，下单只落账不发三方请求，退款只改本地状态。
 */
@Data
@Component
@ConfigurationProperties(prefix = "pay")
public class PayProperties {

	/** 支付总开关；关闭则不调用任何三方渠道 */
	private Boolean enabled = Boolean.FALSE;

	private Wechat wechat = new Wechat();

	private Alipay alipay = new Alipay();

	@Data
	public static class Wechat {

		private Boolean enabled = Boolean.FALSE;

		/** 公众号/小程序/移动应用 appid */
		private String appId;

		/** 商户号 */
		private String mchId;

		/** 商户 API 证书序列号 */
		private String mchSerialNo;

		/** APIv3 密钥 */
		private String apiV3Key;

		/** 商户 API 私钥 PEM 全文（含 -----BEGIN PRIVATE KEY-----） */
		private String privateKey;

		/** 私钥文件路径；与 privateKey 二选一，优先使用该路径 */
		private String privateKeyPath;

		/** 支付结果通知地址（须外网可达） */
		private String notifyUrl;

		/** 退款结果通知地址（须外网可达） */
		private String refundNotifyUrl;
	}

	@Data
	public static class Alipay {

		private Boolean enabled = Boolean.FALSE;

		/** 应用 APPID */
		private String appId;

		/** 应用私钥（PKCS8，可含头尾） */
		private String privateKey;

		/** 支付宝公钥 */
		private String alipayPublicKey;

		/** 网关地址；沙箱为 https://openapi-sandbox.dl.alipaydev.com/gateway.do */
		private String gatewayUrl = "https://openapi.alipay.com/gateway.do";

		private String charset = "UTF-8";

		private String signType = "RSA2";

		private String format = "JSON";

		/** 支付异步通知地址 */
		private String notifyUrl;

		/** 支付完成同步跳转地址（PC 网站支付） */
		private String returnUrl;

		/** 订单相对超时时间，默认 30m */
		private String timeoutExpress = "30m";
	}
}
