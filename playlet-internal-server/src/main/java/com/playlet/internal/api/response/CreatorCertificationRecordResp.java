package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 认证管理：审核记录时间线项。
 */
@Data
@ApiModel(value = "创作者认证审核记录")
public class CreatorCertificationRecordResp {

	private Integer id;
	private String time;
	private String text;
	private Integer status;
	private String statusText;
}
