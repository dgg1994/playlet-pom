package com.playlet.internal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * HTTP 客户端：超时取自 third-party.* yml。
 */
@Configuration
@EnableConfigurationProperties(ThirdPartyProperties.class)
public class RestTemplateConfig {

	private static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
	private static final int DEFAULT_READ_TIMEOUT_MS = 10000;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder, ThirdPartyProperties thirdPartyProperties) {
		int connectTimeout = thirdPartyProperties.getConnectTimeoutMs() == null
				? DEFAULT_CONNECT_TIMEOUT_MS : thirdPartyProperties.getConnectTimeoutMs();
		int readTimeout = thirdPartyProperties.getReadTimeoutMs() == null
				? DEFAULT_READ_TIMEOUT_MS : thirdPartyProperties.getReadTimeoutMs();
		return builder
				.setConnectTimeout(Duration.ofMillis(connectTimeout))
				.setReadTimeout(Duration.ofMillis(readTimeout))
				.build();
	}
}
