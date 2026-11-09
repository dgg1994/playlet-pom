package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 最新发布剧本。
 */
@Data
@ApiModel(value = "最新发布剧本")
public class ScriptLatestRespEntity {

	@ApiModelProperty("剧本ID")
	private Integer id;

	@ApiModelProperty("剧本名称")
	private String title;

	@ApiModelProperty("封面签名URL")
	private String coverUrl;

	@ApiModelProperty("发布时间")
	private Date publishedAt;

	@ApiModelProperty("发布时间文案")
	private String publishedAtText;
}
