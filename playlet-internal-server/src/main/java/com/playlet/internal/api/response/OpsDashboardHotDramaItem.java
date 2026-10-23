package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 运营看板热播短剧排行项。
 */
@Data
@ApiModel(value = "热播短剧排行项", description = "TOP 榜单行")
public class OpsDashboardHotDramaItem {

	@ApiModelProperty("排名 1-based")
	private Integer rankNo;

	@ApiModelProperty("短剧ID")
	private Integer dramaId;

	@ApiModelProperty("短剧名称")
	private String dramaName;

	@ApiModelProperty("分类标签")
	private String dramaTag;

	@ApiModelProperty("播放次数")
	private Long playCount;

	@ApiModelProperty("播放量文案，如 1284.6万")
	private String playCountText;

	@ApiModelProperty("今日充值(元)")
	private BigDecimal todayRecharge;
}
