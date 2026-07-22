package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("签到月历")
public class SignInCalendarRespEntity {

	@ApiModelProperty("年月 yyyy-MM")
	private String yearMonth;

	@ApiModelProperty("今日 yyyy-MM-dd")
	private String today;

	@ApiModelProperty("补签卡余额")
	private Integer makeupCardBalance;

	@ApiModelProperty("每次补签消耗卡数")
	private Integer makeupCostCard;

	@ApiModelProperty("本月已购补签卡张数")
	private Integer makeupBuyUsed;

	@ApiModelProperty("每月限购张数")
	private Integer makeupBuyMonthLimit;

	@ApiModelProperty("购买单价（金币）")
	private Integer makeupBuyPriceCoin;

	@ApiModelProperty("每日状态列表")
	private List<SignInCalendarDayEntity> days;
}
