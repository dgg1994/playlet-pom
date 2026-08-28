package com.playlet.internal.service;

import com.playlet.internal.api.request.WalletWebhookNotifyRequest;
import com.playlet.internal.api.response.WalletWebhookNotifyResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * worldPay WebHook 回调入口。
 */
@RequestMapping("/wallet/webhook")
@Api(value = "钱包Webhook", tags = "钱包Webhook")
public interface WalletWebhookService {

	@PostMapping("/notify")
	@ApiOperation(value = "worldPay回调通知", notes = "无需登录；验签后按 eventType 分发；成功返回 code=200")
	WalletWebhookNotifyResponse notify(@RequestBody WalletWebhookNotifyRequest body, HttpServletRequest request);
}
