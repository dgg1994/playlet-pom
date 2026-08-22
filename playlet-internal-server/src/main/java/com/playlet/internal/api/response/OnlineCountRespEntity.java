package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 当前在线设备数（Redis 心跳窗口内，含未登录）。
 */
@Data
@ApiModel(value = "在线人数", description = "最近窗口内有心跳的独立设备数")
public class OnlineCountRespEntity {

	@ApiModelProperty("当前在线设备数")
	private Long onlineCount;

	@ApiModelProperty("在线判定窗口秒数")
	private Long windowSeconds;

	@ApiModelProperty("服务端当前时间毫秒")
	private Long serverTimeMs;
}
