package com.playlet.oversea.api.response;

import com.playlet.oversea.entity.drama.DramaAssetAuditStepEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "集评审列表项（含步骤）", description = "集基础信息 + 该集 AI/A/B steps")
public class DramaAssetAuditEpisodeRespEntity extends DramaWorkAuditListRespEntity {

	@ApiModelProperty("集评审步骤：AI / A组 / B组")
	private List<DramaAssetAuditStepEntity> steps;
}
