package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 作家端作品数据分析列表行。
 */
@Data
@ApiModel(value = "作品数据分析行", description = "曝光/有效播放/完播/评分")
public class DramaAnalyticsRespEntity {

	@ApiModelProperty("剧ID")
	private Integer id;

	@ApiModelProperty("短剧标题")
	private String dramaTitle;

	@ApiModelProperty("封面（已签名）")
	private String coverUrl;

	@ApiModelProperty("总集数")
	private Integer totalEpisodes;

	@ApiModelProperty("热度")
	private Long hotScore;

	@ApiModelProperty("曝光量（各集合计）")
	private Long exposure;

	@ApiModelProperty("有效播放量（日表 play_pv 合计）")
	private Long playPv;

	@ApiModelProperty("完播量（各集合计）")
	private Long complete;

	@ApiModelProperty("完播率（百分比，一位小数）")
	private BigDecimal completeRate;

	@ApiModelProperty("当前评分")
	private BigDecimal scoreNum;

	@ApiModelProperty("累计收益金币（结算未按剧入账时为0）")
	private Long incomeCoin;

	@ApiModelProperty("创建时间")
	private Date setTime;
}
