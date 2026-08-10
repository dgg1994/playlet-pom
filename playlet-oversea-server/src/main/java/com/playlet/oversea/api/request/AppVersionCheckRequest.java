package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("C端版本检查请求")
public class AppVersionCheckRequest {

	@ApiModelProperty(value = "平台：android / ios / web / windows / mac", required = true, example = "android")
	private String platform;

	@ApiModelProperty(value = "渠道：default / googleplay / appstore 等", example = "default")
	private String channel;

	@ApiModelProperty(value = "客户端当前版本号整数", required = true, example = "10200")
	private Integer versionCode;

	@ApiModelProperty(value = "客户端当前版本名（可选）", example = "1.2.0")
	private String versionName;
}
