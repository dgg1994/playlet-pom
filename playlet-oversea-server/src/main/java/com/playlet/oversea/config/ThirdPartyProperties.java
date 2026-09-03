package com.playlet.oversea.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * worldPay 等三方网关配置，前缀 third-party。
 */
@Data
@Component
@ConfigurationProperties(prefix = "third-party")
public class ThirdPartyProperties {

	/** 网关根地址 */
	private String baseUrl = "";

	/** 商户 appId */
	private String appId = "";

	/** RSA 私钥（PKCS8 / PKCS1 Base64） */
	private String privateKey = "";

	/** RSA 公钥（验 WebHook 签名） */
	private String publicKey = "";

	/** 是否校验 WebHook 签名（未配置公钥时可关闭） */
	private boolean webhookSignVerifyEnabled = false;

	/** HTTP 连接超时（毫秒） */
	private Integer connectTimeoutMs = 10000;

	/** HTTP 读超时（毫秒）；关卡/充值等三方接口可能较慢 */
	private Integer readTimeoutMs = 60000;

	/** 17track 物流 API Key（对齐 worldpay EMS_API_KEY） */
	private String emsApiKey = "";

}
