package com.playlet.internal.response.drama;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 剧集视频七牛前端直传凭证。
 */
@Data
@ApiModel("剧集视频七牛直传凭证")
public class DramaVideoUploadResp {

	@ApiModelProperty(value = "七牛 UploadToken", required = true)
	private String uploadToken;

	@ApiModelProperty(value = "对象 key，直传时必须使用", required = true)
	private String key;

	@ApiModelProperty("访问域名")
	private String domain;

	@ApiModelProperty("token 有效期（秒）")
	private Long expireSeconds;

	@ApiModelProperty("七牛上传入口")
	private String uploadUrl;
}
