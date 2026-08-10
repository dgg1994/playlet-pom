package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("指定分辨率下载地址")
public class VideoDownloadUrlResp {

	@ApiModelProperty("剧集资源ID")
	private Integer assetId;

	@ApiModelProperty("清晰度：360/480/720/1080")
	private String definition;

	@ApiModelProperty("展示名：流畅/标清/高清/超清")
	private String label;

	@ApiModelProperty("七牛对象路径/key，如 oceans_720.mp4")
	private String path;

	@ApiModelProperty("已签名下载地址（MP4，可直接下载）")
	private String downloadUrl;
}
