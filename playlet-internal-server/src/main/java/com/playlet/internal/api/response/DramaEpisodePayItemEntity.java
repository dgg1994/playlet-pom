package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 短剧付费详情-单集付费统计项。
 */
@Data
@ApiModel(value = "剧集付费项", description = "按集统计购买与收入")
public class DramaEpisodePayItemEntity {

	@ApiModelProperty("集ID")
	private Integer assetId;

	@ApiModelProperty("集数")
	private Integer setNum;

	@ApiModelProperty("集标题")
	private String episodeTitle;

	@ApiModelProperty("解锁类型 0免费 1广告 2付费")
	private Integer unlockType;

	@ApiModelProperty("定价(元)")
	private BigDecimal priceYuan;

	@ApiModelProperty("购买人次")
	private Integer purchaseCount;

	@ApiModelProperty("收入(元)")
	private BigDecimal revenue;

	@ApiModelProperty("收入占比(%)")
	private BigDecimal revenueRate;

	@ApiModelProperty("上架时间")
	private Date shelfTime;
}
