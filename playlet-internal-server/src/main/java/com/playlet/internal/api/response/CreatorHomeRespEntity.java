package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 创作者首页聚合响应。
 */
@Data
@ApiModel(value = "创作者首页", description = "概览 + 热点剧 + 热点题材 + 公告 + 默认影响力榜")
public class CreatorHomeRespEntity {

	@ApiModelProperty("顶部数据概览")
	private CreatorHomeStatsRespEntity stats;

	@ApiModelProperty("近期热点剧集")
	private List<CreatorHomeHotDramaRespEntity> hotDramas = new ArrayList<>();

	@ApiModelProperty("近期热点题材")
	private List<CreatorHomeHotTagRespEntity> hotTags = new ArrayList<>();

	@ApiModelProperty("系统公告摘要")
	private List<CreatorHomeNoticeRespEntity> notices = new ArrayList<>();
}
