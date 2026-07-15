package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("剧场首页")
public class TheaterHomeRespEntity {

	@ApiModelProperty("榜单分块列表")
	private List<TheaterRankBlockEntity> blocks = new ArrayList<>();
}
