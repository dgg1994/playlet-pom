package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 创作者首页系统公告摘要。
 */
@Data
@ApiModel(value = "创作者首页公告", description = "系统公告列表摘要")
public class CreatorHomeNoticeRespEntity {

	@ApiModelProperty("发布单ID")
	private Long id;

	@ApiModelProperty("标题")
	private String title;

	@ApiModelProperty("发布时间")
	private Date setTime;
}
