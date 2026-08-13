package com.playlet.internal.api.response;

import com.playlet.internal.entity.drama.DramaEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "剧评审详情", description = "剧信息（含 dramaSteps）+ 集列表（每集含各自 steps）")
public class DramaAuditDetailRespEntity {

	@ApiModelProperty("短剧信息（含封面、名称、简介、dramaSteps）")
	private DramaEntity drama;

	@ApiModelProperty("该剧下短剧集列表（每集含独立 steps）")
	private List<DramaAssetAuditEpisodeRespEntity> episodeList;
}
