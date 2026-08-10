package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("多码率播放地址")
public class VideoPlayUrlResp {

	@ApiModelProperty("剧集资源ID")
	private Integer assetId;

	@ApiModelProperty("默认清晰度：360/480/720/1080")
	private String defaultDefinition;

	@ApiModelProperty("多码率列表")
	private List<StreamItem> streams;

	@Data
	@ApiModel("单路清晰度")
	public static class StreamItem {

		@ApiModelProperty("清晰度：360/480/720/1080")
		private String definition;

		@ApiModelProperty("展示名：流畅/标清/高清/超清")
		private String label;

		@ApiModelProperty("已签名播放地址")
		private String videoUrl;
	}
}
