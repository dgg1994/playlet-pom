package com.playlet.internal.service.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playlet.internal.api.request.OnePayBindVerifyRequest;
import com.playlet.internal.config.OnePayProperties;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * OnePay 账号三方校验与账号脱敏。
 */
@Slf4j
@Component
public class OnePayVerifyClient {

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private OnePayProperties onePayProperties;

	/**
	 * 调 OnePay 校验账号，成功返回 openid，失败返回空。
	 */
	public String verifyAccount(OnePayBindVerifyRequest query, Integer uid, String onepayAccount) {
		if (StringUtils.isEmpty(onePayProperties.getBindVerifyUrl())) {
			log.warn("onepay bind-verify-url empty uid={}", uid);
			return null;
		}
		ResponseEntity<String> result = restTemplate.postForEntity(
				onePayProperties.getBindVerifyUrl(), query, String.class);
		if (result.getStatusCode() != HttpStatus.OK) {
			log.warn("onepay bind verify http failed uid={} account={} status={}",
					uid, maskAccount(onepayAccount), result.getStatusCode());
			return null;
		}
		String body = result.getBody();
		if (StringUtils.isEmpty(body)) {
			return null;
		}
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(body);
			JsonNode codeNode = jsonNode.get("code");
			if (codeNode != null && !codeNode.isNull()
					&& codeNode.asInt() != Constants.HTTP_RES_CODE_200) {
				return null;
			}
			JsonNode data = jsonNode.get("data");
			if (data == null || data.isNull()) {
				return null;
			}
			JsonNode openidNode = data.get("openid");
			if (openidNode == null || openidNode.isNull()) {
				return null;
			}
			String openid = openidNode.asText();
			return StringUtils.isEmpty(openid) ? null : openid;
		} catch (Exception e) {
			log.error("onepay bind parse failed uid={} account={}", uid, maskAccount(onepayAccount), e);
			return null;
		}
	}

	public static String maskAccount(String account) {
		if (StringUtils.isEmpty(account)) {
			return "***";
		}
		if (account.contains("@")) {
			int at = account.indexOf('@');
			String name = account.substring(0, at);
			String domain = account.substring(at);
			if (name.length() <= 1) {
				return "*" + domain;
			}
			return name.charAt(0) + "***" + domain;
		}
		if (account.length() < 6) {
			return "***";
		}
		return account.substring(0, 2) + "***" + account.substring(account.length() - 2);
	}
}
