package com.playlet.internal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * HTTP 客户端：超时取自 onepay.* yml。
 */
@Configuration
@EnableConfigurationProperties(OnePayProperties.class)
public class RestTemplateConfig {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder, OnePayProperties onePayProperties) {
		return builder
				.setConnectTimeout(Duration.ofMillis(onePayProperties.getConnectTimeoutMs()))
				.setReadTimeout(Duration.ofMillis(onePayProperties.getReadTimeoutMs()))
				.build();
	}
}
