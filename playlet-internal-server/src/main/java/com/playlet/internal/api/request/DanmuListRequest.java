package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 弹幕列表入参（按播放时间窗）。
 */
@Data
@ApiModel(value = "弹幕列表", description = "按集+时间窗拉取弹幕")
public class DanmuListRequest {

	@ApiModelProperty(value = "集ID，drama_asset.id", required = true)
	private Integer videoId;

	@ApiModelProperty(value = "时间窗起点秒（含）", required = true)
	private Integer fromTime;

	@ApiModelProperty(value = "时间窗终点秒（含）", required = true)
	private Integer toTime;
}
