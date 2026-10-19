package com.playlet.internal.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * HTTP 客户端默认超时。
 */
@Configuration
public class RestTemplateConfig {

	private static final int DEFAULT_CONNECT_TIMEOUT_MS = 10000;
	private static final int DEFAULT_READ_TIMEOUT_MS = 60000;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder
				.setConnectTimeout(Duration.ofMillis(DEFAULT_CONNECT_TIMEOUT_MS))
				.setReadTimeout(Duration.ofMillis(DEFAULT_READ_TIMEOUT_MS))
				.build();
	}
}
