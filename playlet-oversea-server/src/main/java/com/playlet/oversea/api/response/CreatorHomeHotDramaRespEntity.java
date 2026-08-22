package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创作者首页近期热点剧。
 */
@Data
@ApiModel(value = "创作者首页热点剧", description = "海报 + 标题")
public class CreatorHomeHotDramaRespEntity {

	@ApiModelProperty("剧ID")
	private Integer dramaId;

	@ApiModelProperty("标题")
	private String dramaTitle;

	@ApiModelProperty("封面（已签名）")
	private String coverUrl;
}
