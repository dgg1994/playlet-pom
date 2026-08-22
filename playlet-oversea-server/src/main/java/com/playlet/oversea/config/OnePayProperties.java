package com.playlet.oversea.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OnePay 三方配置，对应 yml 前缀 onepay。
 */
@Data
@ConfigurationProperties(prefix = "onepay")
public class OnePayProperties {

	/** 绑定校验接口地址（POST JSON） */
	private String bindVerifyUrl = "";

	/** 提现打款接口地址（POST JSON）；空则走 mock */
	private String withdrawUrl = "";

	/** 连接超时毫秒 */
	private int connectTimeoutMs = 5000;

	/** 读超时毫秒 */
	private int readTimeoutMs = 10000;
}
