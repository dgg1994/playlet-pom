package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 微信绑定二维码返回。
 */
@Data
@ApiModel(value = "微信绑定二维码")
public class CreatorWechatBindResp {

	private String scene;
	private String qrCodeUrl;
	private Long expireSeconds;
}
