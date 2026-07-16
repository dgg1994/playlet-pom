package com.playlet.internal.api.response;

import com.playlet.internal.entity.drama.RankListEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("剧场首页分块")
public class TheaterRankBlockEntity {

	@ApiModelProperty("榜分组ID")
	private String groupId;

	@ApiModelProperty("榜展示名")
	private String boardName;

	@ApiModelProperty("1算法 2人工")
	private Integer boardType;

	@ApiModelProperty("预览条目")
	private List<RankListEntity> items = new ArrayList<>();
}
