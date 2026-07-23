package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("短剧评论评分汇总")
public class DramaCommentScoreSummaryEntity {

	@ApiModelProperty("剧ID")
	private Integer dramaId;

	@ApiModelProperty("平均分（保留1位小数）")
	private Double avgScore;

	@ApiModelProperty("评分人数")
	private Integer scoreCount;
}
