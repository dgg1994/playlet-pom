package com.playlet.oversea.query.drama;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("剧集视频直传凭证请求")
public class DramaVideoUploadTokenQuery {

	@NotNull(message = "短剧id不能为空")
	@ApiModelProperty(value = "短剧 id", required = true)
	private Integer dramaId;

	@NotNull(message = "当前集数不能为空")
	@ApiModelProperty(value = "第几集", required = true)
	private Integer setNum;

	@ApiModelProperty(value = "视频扩展名，默认 mp4；允许 mp4/mov/m4v/webm/mkv/avi/m3u8/ts", required = false)
	private String ext;
}
