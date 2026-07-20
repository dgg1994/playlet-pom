package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("福利任务项")
public class WelfareTaskItemEntity {

	@ApiModelProperty("任务ID")
	private Integer taskId;

	@ApiModelProperty("任务编码，如 WATCH_EP")
	private String taskCode;

	@ApiModelProperty("任务名称")
	private String taskName;

	@ApiModelProperty("任务说明")
	private String taskDesc;

	@ApiModelProperty("图标")
	private String taskIcon;

	@ApiModelProperty("基础奖励金币")
	private Integer rewardCoin;

	@ApiModelProperty("广告加赠金币，0不支持")
	private Integer adBoostCoin;

	@ApiModelProperty("周期：1日 2一次 3周 4月")
	private Integer cycleType;

	@ApiModelProperty("达标目标")
	private Integer targetCount;

	@ApiModelProperty("当前进度")
	private Integer progress;

	@ApiModelProperty("-1未领取任务 0进行中 1可领奖励 2奖励已领 3已过期 4已放弃，见 WelfareProgressStatusEnums")
	private Integer progressStatus;

	@ApiModelProperty("是否已领取本周期任务（可开始累计）")
	private Boolean accepted;

	@ApiModelProperty("行为类型 WATCH/SHARE/FOLLOW...")
	private String actionType;

	@ApiModelProperty("是否自动发奖")
	private Integer autoClaim;
}
