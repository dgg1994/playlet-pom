package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("C端版本检查结果")
public class AppVersionCheckRespEntity {

	@ApiModelProperty("是否需要更新")
	private Boolean needUpdate;

	@ApiModelProperty("是否强制更新")
	private Boolean forceUpdate;

	@ApiModelProperty("最新版本号整数")
	private Integer versionCode;

	@ApiModelProperty("最新版本名")
	private String versionName;

	@ApiModelProperty("下载/商店地址")
	private String downloadUrl;

	@ApiModelProperty("更新标题")
	private String title;

	@ApiModelProperty("更新说明")
	private String content;
}
