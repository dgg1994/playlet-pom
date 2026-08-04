package com.playlet.internal.query.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("极光推送开关")
public class PushSwitchQuery {

	@ApiModelProperty(name = "enabled", value = "1开启 0关闭", required = true, dataType = "Integer")
	private Integer enabled;
}
