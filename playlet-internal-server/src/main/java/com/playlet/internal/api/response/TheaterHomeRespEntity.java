package com.playlet.internal.api.response;

import com.playlet.internal.entity.drama.DramaEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("剧场首页")
public class TheaterHomeRespEntity {

	@ApiModelProperty("当前 Tab：hot / playlist / recommend")
	private String tab;

	@ApiModelProperty("推荐 Tab 是否应用了偏好加权")
	private Boolean preferApplied;

	@ApiModelProperty("短剧推荐轮播（热榜可用）")
	private List<DramaEntity> carousels = new ArrayList<>();

	@ApiModelProperty("榜单/剧单分块预览")
	private List<TheaterRankBlockEntity> blocks = new ArrayList<>();

	@ApiModelProperty("推荐信息流（仅 tab=recommend）")
	private RecommendPageRespEntity feed;
}
