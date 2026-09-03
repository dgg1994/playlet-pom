package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 物流轨迹节点。
 */
@Data
@ApiModel(value = "物流轨迹节点", description = "17track 事件摘要")
public class WalletLogisticsEventResp {

	@ApiModelProperty("描述")
	private String description;

	@ApiModelProperty("地点")
	private String location;

	@ApiModelProperty("UTC 时间")
	private String timeUtc;

	@ApiModelProperty("子状态文案")
	private String subStatus;

	@ApiModelProperty("子状态 id")
	private Integer subStatusId;
}
