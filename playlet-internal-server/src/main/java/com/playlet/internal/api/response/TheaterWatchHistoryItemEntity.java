package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("剧场浏览历史条目")
public class TheaterWatchHistoryItemEntity {

	@ApiModelProperty("短剧业务ID")
	private String dramaId;

	@ApiModelProperty("标题")
	private String title;

	@ApiModelProperty("封面")
	private String coverUrl;

	@ApiModelProperty("总集数")
	private Integer totalEpisodes;

	@ApiModelProperty("是否完结 1是0否")
	private Integer finished;

	@ApiModelProperty("最近观看分集业务ID")
	private String episodeId;

	@ApiModelProperty("最近观看集序号")
	private Integer episodeNo;

	@ApiModelProperty("分集内播放进度（秒）")
	private Integer watchProgress;

	@ApiModelProperty("最近观看时间")
	private Date gmtModified;
}
