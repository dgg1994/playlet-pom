package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("观影礼阶梯项")
public class WatchGiftRewardItemEntity {

	@ApiModelProperty("档位序号")
	private Integer gearIndex;

	@ApiModelProperty("达标秒数")
	private Integer targetSeconds;

	@ApiModelProperty("达标分钟（展示用）")
	private Integer targetMinutes;

	@ApiModelProperty("奖励金币")
	private Integer rewardCoin;

	@ApiModelProperty("状态：done已领 / claimable可领 / locked未达")
	private String state;
}
