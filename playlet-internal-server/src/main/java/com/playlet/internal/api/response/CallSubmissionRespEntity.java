package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 征稿详情-已投稿剧本。
 */
@Data
@ApiModel(value = "征稿已投稿剧本")
public class CallSubmissionRespEntity {

	@ApiModelProperty(name = "title", value = "剧本名称")
	private String title;

	@ApiModelProperty(name = "user", value = "投稿人昵称")
	private String user;

	@ApiModelProperty(name = "time", value = "投稿日期 yyyy-MM-dd")
	private String time;

	@ApiModelProperty(name = "intro", value = "剧本简介")
	private String intro;

	@ApiModelProperty(name = "attachments", value = "稿件附件 [{name,key}]")
	private List<Map<String, Object>> attachments;
}
