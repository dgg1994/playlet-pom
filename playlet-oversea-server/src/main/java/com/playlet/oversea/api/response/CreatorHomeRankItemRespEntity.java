package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创作者首页榜单行。
 */
@Data
@ApiModel(value = "创作者首页榜单行", description = "影响力/成长力")
public class CreatorHomeRankItemRespEntity {

	@ApiModelProperty("名次 1 起")
	private Integer rankNo;

	@ApiModelProperty("作家ID")
	private Integer creatorId;

	@ApiModelProperty("作者昵称")
	private String nickname;

	@ApiModelProperty("分值：影响力=近窗有效观看秒；成长力=倍率×100 取整")
	private Long score;
}
