package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("剧场点赞列表条目")
public class TheaterLikeItemEntity {

	@ApiModelProperty("短剧ID")
	private Integer dramaId;

	@ApiModelProperty("标题")
	private String title;

	@ApiModelProperty("封面")
	private String coverUrl;

	@ApiModelProperty("总集数")
	private Integer totalEpisodes;

	@ApiModelProperty("是否完结 1是0否")
	private Integer finished;

	@ApiModelProperty("1整剧点赞 2单集点赞")
	private Integer likeType;

	@ApiModelProperty("分集ID；整剧点赞时为空")
	private String episodeId;

	@ApiModelProperty("点赞时间")
	private Date setTime;
}
