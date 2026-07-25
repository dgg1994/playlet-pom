package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 榜单条目 + drama 联查展示字段（C 端首页预览 / 管理端列表详情）
 */
@Data
@ApiModel("榜单条目（含剧信息）")
public class RankListItemEntity {

	@ApiModelProperty("条目主键")
	private Integer id;

	@ApiModelProperty("所属榜 groupId")
	private String boardGroupId;

	@ApiModelProperty("名次")
	private Integer rankNo;

	@ApiModelProperty("短剧ID")
	private String dramaId;

	@ApiModelProperty("1生效0停用")
	private Integer status;

	@ApiModelProperty("标题（drama）")
	private String title;

	@ApiModelProperty("封面（drama）")
	private String coverUrl;

	@ApiModelProperty("热度文案（drama）")
	private String hotScoreText;

	@ApiModelProperty("热度值（drama）")
	private Long hotScore;

	@ApiModelProperty("总集数（drama）")
	private Integer totalEpisodes;

	@ApiModelProperty("1完结0连载（drama.finished_state）")
	private Integer finished;

	@ApiModelProperty("创建时间")
	private Date setTime;

	@ApiModelProperty("更新时间")
	private Date gmtModified;
}
