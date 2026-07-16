package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("剧场收藏条目")
public class TheaterCollectItemEntity {

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

	@ApiModelProperty("收藏时间")
	private Date setTime;
}
