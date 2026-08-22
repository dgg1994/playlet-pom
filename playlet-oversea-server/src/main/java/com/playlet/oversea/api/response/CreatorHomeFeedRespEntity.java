package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 创作者首页 Feed：热点剧 + 热点题材。
 */
@Data
@ApiModel(value = "创作者首页Feed", description = "近期热点剧集与热点题材（不含公告）")
public class CreatorHomeFeedRespEntity {

	@ApiModelProperty("近期热点剧集")
	private List<CreatorHomeHotDramaRespEntity> hotDramas = new ArrayList<>();

	@ApiModelProperty("近期热点题材")
	private List<CreatorHomeHotTagRespEntity> hotTags = new ArrayList<>();
}
