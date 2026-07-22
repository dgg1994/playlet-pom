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

	@ApiModelProperty("签到摘要，未开启或未配置时为 null")
	private SignInHomeSummaryEntity signIn;

	@ApiModelProperty("任务列表")
	private List<WelfareTaskItemEntity> tasks;
}
