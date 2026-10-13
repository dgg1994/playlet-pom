package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 发送弹幕入参。
 */
@Data
@ApiModel(value = "发送弹幕", description = "用户发送弹幕请求")
public class DanmuSendRequest {

	@ApiModelProperty(value = "集ID，drama_asset.id", required = true)
	private Integer videoId;

	@ApiModelProperty(value = "弹幕正文", required = true)
	private String content;

	@ApiModelProperty(value = "出现在视频中的秒数（从0起）", required = true)
	private Integer playTime;

	@ApiModelProperty(value = "颜色，如 #FFFFFF；不传用默认白")
	private String color;

	@ApiModelProperty(value = "1滚动 2顶部 3底部；不传默认滚动")
	private Integer mode;

	@ApiModelProperty(value = "1标准 2加大；不传默认标准")
	private Integer fontSize;
}
