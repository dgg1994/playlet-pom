package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 剧集视频登记（/dramaAsset/release）成功响应。
 */
@Data
@ApiModel("剧集视频登记结果")
public class DramaAssetReleaseRespEntity {

	@ApiModelProperty(value = "剧集资源主键 id", required = true)
	private Integer id;

	@ApiModelProperty(value = "库内对象 key（默认清晰度 m3u8）", required = true)
	private String key;

	@ApiModelProperty(value = "可预览/播放的签名 URL", required = true)
	private String videoUrl;
}
