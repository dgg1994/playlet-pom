package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 管理端用户观看记录行。
 */
@Data
@ApiModel(value = "观看记录行", description = "用户观看历史")
public class AppUserWatchHistoryItemEntity {

	@ApiModelProperty("短剧ID")
	private Integer dramaId;

	@ApiModelProperty("短剧名")
	private String dramaName;

	@ApiModelProperty("最近观看集ID")
	private String episodeId;

	@ApiModelProperty("进度秒")
	private Integer watchProgress;

	@ApiModelProperty("最近观看时间")
	private Date gmtModified;
}
