package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("勋章弹窗确认请求")
public class MedalAckNotifyRequest {

	@ApiModelProperty(value = "勋章ID列表，支持单条或批量", required = true, example = "[1,2,3]")
	private List<Integer> medalIds;
}
