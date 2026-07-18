package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("福利首页")
public class WelfareHomeRespEntity {

	@ApiModelProperty("金币余额")
	private Long coinBalance;

	@ApiModelProperty("任务列表")
	private List<WelfareTaskItemEntity> tasks;
}
