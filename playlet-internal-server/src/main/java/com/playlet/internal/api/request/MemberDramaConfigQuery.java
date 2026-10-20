package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理端：短剧会员属性配置入参。
 */
@Data
@ApiModel(value = "会员短剧配置", description = "设置是否会员短剧")
public class MemberDramaConfigQuery {

	@ApiModelProperty(value = "短剧ID", required = true)
	private Integer dramaId;

	@ApiModelProperty(value = "是否会员短剧 0否 1是", required = true)
	private Integer isMemberDrama;
}
