package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理端向用户发送站内通知。
 */
@Data
@ApiModel(value = "发送站内通知", description = "写入用户收件箱并推送")
public class AppUserNotifyQuery {

	@ApiModelProperty(value = "用户ID", required = true)
	private Integer uid;

	@ApiModelProperty(value = "标题", required = true)
	private String title;

	@ApiModelProperty(value = "正文", required = true)
	private String content;
}
