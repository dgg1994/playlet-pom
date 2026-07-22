package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("福利首页-签到摘要")
public class SignInHomeSummaryEntity {

	@ApiModelProperty("今日自然日 yyyy-MM-dd")
	private String today;

	@ApiModelProperty("今日是否已签")
	private Boolean todaySigned;

	@ApiModelProperty("当前连续天数")
	private Integer streakDays;

	@ApiModelProperty("累计签到天数（含补签）")
	private Integer totalSignDays;

	@ApiModelProperty("今日签到可得/已得金币")
	private Integer todayRewardCoin;

	@ApiModelProperty("今日对应奖励档位 dayIndex")
	private Integer todayRewardDayIndex;

	@ApiModelProperty("本月剩余补签次数；null 表示不限制次数（仅受补签卡约束）")
	private Integer makeupRemainCount;

	@ApiModelProperty("是否开放补签")
	private Boolean makeupEnabled;

	@ApiModelProperty("补签卡余额")
	private Integer makeupCardBalance;

	@ApiModelProperty("每次补签消耗卡数")
	private Integer makeupCostCard;

	@ApiModelProperty("本月已购补签卡")
	private Integer makeupBuyUsed;

	@ApiModelProperty("每月限购补签卡张数")
	private Integer makeupBuyMonthLimit;

	@ApiModelProperty("购买单价金币，0不开放")
	private Integer makeupBuyPriceCoin;

	@ApiModelProperty("奖励阶梯")
	private List<SignInRewardItemEntity> rewards;
}
