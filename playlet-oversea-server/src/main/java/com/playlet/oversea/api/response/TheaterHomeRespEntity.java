package com.playlet.oversea.api.response;

import com.playlet.oversea.entity.drama.DramaEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("剧场首页")
public class TheaterHomeRespEntity {

	@ApiModelProperty("短剧推荐轮播")
	private List<DramaEntity> carousels = new ArrayList<>();

	@ApiModelProperty("榜单分块预览（条目联查 drama）")
	private List<TheaterRankBlockEntity> blocks = new ArrayList<>();
}
