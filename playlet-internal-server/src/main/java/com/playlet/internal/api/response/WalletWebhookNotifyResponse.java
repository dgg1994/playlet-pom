package com.playlet.internal.api.response;

import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.WalletWebhookConstants;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * worldPay WebHook 成功响应（文档约定格式）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "钱包Webhook响应", description = "code=200 表示接收成功")
public class WalletWebhookNotifyResponse {

	private Integer code;

	private String msg;

	public static WalletWebhookNotifyResponse success() {
		return new WalletWebhookNotifyResponse(Constants.HTTP_RES_CODE_200, WalletWebhookConstants.ACK_MSG);
	}

	public static WalletWebhookNotifyResponse fail(Integer code, String msg) {
		return new WalletWebhookNotifyResponse(code, msg);
	}
}
