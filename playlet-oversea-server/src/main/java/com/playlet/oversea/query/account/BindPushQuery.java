package com.playlet.oversea.query.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * App 启动后绑定极光推送设备信息（支持未登录）
 */
@Data
@ApiModel("绑定极光推送")
public class BindPushQuery {

	@ApiModelProperty(name = "cid", value = "极光推送设备标识（客户端常用，等价 registrationId）", required = false, dataType = "String")
	private String cid;

	@ApiModelProperty(name = "registrationId", value = "极光 registrationId（与 cid 二选一，优先 registrationId）", required = false, dataType = "String")
	private String registrationId;

	@ApiModelProperty(name = "deviceName", value = "设备名称，可选", required = false, dataType = "String")
	private String deviceName;
}
