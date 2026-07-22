package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("签到奖励阶梯项")
public class SignInRewardItemEntity {

	@ApiModelProperty("连续第N天（档位）")
	private Integer dayIndex;

	@ApiModelProperty("奖励金币")
	private Integer rewardCoin;

	@ApiModelProperty("状态，见 SignInRewardStateEnums：done已领 / today今日档 / locked未达")
	private String state;
}
