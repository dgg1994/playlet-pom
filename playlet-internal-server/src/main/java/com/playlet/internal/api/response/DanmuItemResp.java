package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 弹幕列表项。
 */
@Data
@ApiModel(value = "弹幕项", description = "播放器展示用弹幕")
public class DanmuItemResp {

	@ApiModelProperty("弹幕ID")
	private Long id;

	@ApiModelProperty("发送用户ID")
	private Integer uid;

	@ApiModelProperty("发送者昵称")
	private String nickname;

	@ApiModelProperty("发送者头像（已签名）")
	private String avatar;

	@ApiModelProperty("正文")
	private String content;

	@ApiModelProperty("出现秒数")
	private Integer playTime;

	@ApiModelProperty("颜色")
	private String color;

	@ApiModelProperty("1滚动 2顶部 3底部")
	private Integer mode;

	@ApiModelProperty("1标准 2加大")
	private Integer fontSize;
}
