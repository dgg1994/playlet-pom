package com.playlet.internal.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Telegram 飞机消息发送（对齐 worldpay TelegramNotificationUtil）。
 */
@Slf4j
@Component
public class TelegramNotificationUtil {

	@Value("${telegram.mornitor_bot_token:}")
	private String mornitorBotToken;

	@Value("${telegram.notice_group_id:}")
	private String noticeGroupId;

	private final RestTemplate restTemplate = new RestTemplate();

	/**
	 * 向运营群发送 HTML 消息；失败仅打日志，不影响主流程。
	 */
	public void sendTelegramBindingMsg(String message) {
		if (StringUtils.isEmpty(mornitorBotToken) || StringUtils.isEmpty(noticeGroupId)) {
			log.warn("Telegram 配置缺失，跳过发送");
			return;
		}
		if (StringUtils.isEmpty(message)) {
			log.warn("Telegram 消息为空，跳过发送");
			return;
		}
		try {
			String telegramApiUrl = String.format("https://api.telegram.org/bot%s/sendMessage", mornitorBotToken);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("chat_id", noticeGroupId);
			requestBody.put("text", message);
			requestBody.put("parse_mode", "HTML");
			requestBody.put("disable_web_page_preview", true);
			HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(telegramApiUrl, httpEntity, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				log.info("Telegram 通知发送成功");
			} else {
				log.warn("Telegram 通知发送失败，响应码：{}", response.getStatusCodeValue());
			}
		} catch (Exception e) {
			log.warn("Telegram 通知发送失败: {}", e.getMessage(), e);
		}
	}
}
