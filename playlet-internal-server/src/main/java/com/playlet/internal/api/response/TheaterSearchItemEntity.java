package com.playlet.internal.api.response;

import com.playlet.internal.entity.drama.TagEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("剧场搜索条目")
public class TheaterSearchItemEntity {

	@ApiModelProperty("业务剧ID")
	private String dramaId;

	@ApiModelProperty("标题")
	private String title;

	@ApiModelProperty("封面")
	private String coverUrl;

	@ApiModelProperty("热度值")
	private Long hotScore;

	@ApiModelProperty("热度文案")
	private String hotScoreText;

	@ApiModelProperty("总集数")
	private Integer totalEpisodes;

	@ApiModelProperty("是否完结 1是0否")
	private Integer finished;

	@ApiModelProperty("简介")
	private String description;

	@ApiModelProperty("标签")
	private List<TagEntity> tags = new ArrayList<>();
}
