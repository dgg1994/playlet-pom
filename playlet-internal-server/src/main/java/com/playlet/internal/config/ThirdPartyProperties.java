package com.playlet.internal.config;

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

}
