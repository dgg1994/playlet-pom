package com.playlet.internal.query.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("极光推送开关")
public class PushSwitchQuery {

	@ApiModelProperty(name = "cid", value = "极光设备标识（与 registrationId 二选一）", required = false, dataType = "String")
	private String cid;

	@ApiModelProperty(name = "registrationId", value = "极光 registrationId（优先）", required = false, dataType = "String")
	private String registrationId;

	@ApiModelProperty(name = "enabled", value = "1开启 0关闭", required = true, dataType = "Integer")
	private Integer enabled;
}
