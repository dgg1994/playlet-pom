package com.playlet.internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * USDT 链上监听网关配置，前缀 usdt-topin（对齐 worldpay monitor.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "usdt-topin")
public class UsdtTopinProperties {

	/** 网关根地址，如 https://mornitor.worldpay.club */
	private String gatewayUrl = "";

	/** APIKEY 请求头 */
	private String apiKey = "";

	/** HMAC-SHA256 签名密钥 */
	private String signKey = "";

	/** HTTP 连接超时（毫秒） */
	private int connectTimeoutMs = 10000;

	/** HTTP 读超时（毫秒） */
	private int readTimeoutMs = 30000;

	/** 充值回调 IP 白名单 */
	private List<String> callbackIpWhitelist = new ArrayList<>();
}
