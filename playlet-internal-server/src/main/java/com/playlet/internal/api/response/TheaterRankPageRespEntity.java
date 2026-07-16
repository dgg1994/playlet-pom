package com.playlet.internal.api.response;

import com.github.pagehelper.PageInfo;
import com.playlet.internal.entity.drama.RankListEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("剧场榜单分页")
public class TheaterRankPageRespEntity {

	@ApiModelProperty("榜分组ID")
	private String groupId;

	@ApiModelProperty("榜展示名")
	private String boardName;

	@ApiModelProperty("分页结果")
	private PageInfo<RankListEntity> page;
}
