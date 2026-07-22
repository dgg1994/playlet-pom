package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("福利首页-观影礼摘要")
public class WatchGiftHomeSummaryEntity {

	@ApiModelProperty("今日 yyyy-MM-dd")
	private String today;

	@ApiModelProperty("今日已看秒数")
	private Integer watchSeconds;

	@ApiModelProperty("今日已看分钟（向下取整）")
	private Integer watchMinutes;

	@ApiModelProperty("下一未完成档目标秒数，全部完成则为 null")
	private Integer nextTargetSeconds;

	@ApiModelProperty("阶梯列表")
	private List<WatchGiftRewardItemEntity> rewards;
}
