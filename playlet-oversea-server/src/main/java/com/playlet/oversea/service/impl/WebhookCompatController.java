package com.playlet.oversea.service.impl;

import com.playlet.oversea.api.request.WalletWebhookNotifyRequest;
import com.playlet.oversea.api.response.WalletWebhookNotifyResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 三方 WebHook 兼容入口（对齐 onetoken /webhook/agentNotify）。
 */
@RestController
@CrossOrigin
@RequestMapping("/webhook")
@Api(value = "三方Webhook兼容", tags = "三方Webhook兼容")
public class WebhookCompatController {

	@Autowired
	private WalletWebhookServiceImpl walletWebhookService;

	@PostMapping("/agentNotify")
	@ApiOperation(value = "uCard三方回调", notes = "与 onetoken /webhook/agentNotify 路径一致；转发至 wallet/webhook/notify")
	public WalletWebhookNotifyResponse agentNotify(@RequestBody WalletWebhookNotifyRequest body,
			HttpServletRequest request) {
		return walletWebhookService.notify(body, request);
	}
}
