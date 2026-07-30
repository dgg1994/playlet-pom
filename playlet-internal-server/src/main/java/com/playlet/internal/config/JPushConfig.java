package com.playlet.internal.config;

import com.playlet.internal.utils.JPushUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 极光推送初始化
 */
@Configuration
public class JPushConfig {

	private static final Logger LOG = LoggerFactory.getLogger(JPushConfig.class);

	@Value("${jpush.app_key}")
	private String appKey;

	@Value("${jpush.master_secret}")
	private String masterSecret;

	@Value("${jpush.environment}")
	private boolean environment;

	@PostConstruct
	public void init() {
		LOG.info("开始初始化极光推送配置...");
		try {
			JPushUtils.init(appKey, masterSecret, environment);
			LOG.info("极光推送配置初始化完成");
		} catch (Exception e) {
			LOG.error("极光推送配置初始化失败", e);
		}
	}

	@PreDestroy
	public void destroy() {
		LOG.info("关闭极光推送线程池");
		JPushUtils.shutdown();
	}
}
