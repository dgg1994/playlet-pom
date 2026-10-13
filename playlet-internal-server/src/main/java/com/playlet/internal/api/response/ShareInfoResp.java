package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 播放页内容分享素材。
 */
@Data
@ApiModel(value = "分享素材", description = "播放页分享面板用 title/desc/cover/url")
public class ShareInfoResp {

	@ApiModelProperty("短剧ID")
	private Integer dramaId;

	@ApiModelProperty("集ID；非法或不传时可为 null")
	private Integer videoId;

	@ApiModelProperty("分享标题")
	private String title;

	@ApiModelProperty("分享描述")
	private String desc;

	@ApiModelProperty("封面（已签名）")
	private String coverUrl;

	@ApiModelProperty("可传播落地链接")
	private String shareUrl;

	@ApiModelProperty("当前剧分享量")
	private Long shareScore;
}
