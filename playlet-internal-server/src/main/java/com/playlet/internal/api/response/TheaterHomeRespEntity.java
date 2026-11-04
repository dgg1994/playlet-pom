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

	@ApiModelProperty("短剧推荐轮播（热榜可用；若有 position=2 的投放中 Banner，前端优先用 bannerCarousels）")
	private List<DramaEntity> carousels = new ArrayList<>();

	@ApiModelProperty("Banner 轮播（热榜 position=2 投放中时返回，优先级高于 carousels；无投放中 Banner 则为 null）")
	private List<BannerCarouselItemResp> bannerCarousels;

	@ApiModelProperty("榜单/剧单分块预览")
	private List<TheaterRankBlockEntity> blocks = new ArrayList<>();

	@ApiModelProperty("推荐信息流（仅 tab=recommend）")
	private RecommendPageRespEntity feed;
}
