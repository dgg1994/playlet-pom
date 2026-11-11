package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 征稿广场列表卡片。
 */
@Data
@ApiModel(value = "征稿卡片")
public class CallCardRespEntity {

	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@ApiModelProperty(name = "title", value = "征稿标题")
	private String title;

	@ApiModelProperty(name = "topic", value = "题材标签")
	private String topic;

	@ApiModelProperty(name = "exclusive", value = "是否独家")
	private Boolean exclusive;

	@ApiModelProperty(name = "amount", value = "总奖金（元）")
	private Long amount;

	@ApiModelProperty(name = "participants", value = "已参与人数")
	private Integer participants;

	@ApiModelProperty(name = "need", value = "需求稿件数")
	private Integer need;

	@ApiModelProperty(name = "deadline", value = "截止日期 yyyy-MM-dd")
	private String deadline;

	@ApiModelProperty(name = "createdAt", value = "发布日期 yyyy-MM-dd")
	private String createdAt;

	@ApiModelProperty(name = "tags", value = "卡片标签")
	private List<String> tags;

	@ApiModelProperty(name = "desc", value = "卡片摘要")
	private String desc;

	@ApiModelProperty(name = "benchmark", value = "对标作品")
	private String benchmark;

	@ApiModelProperty(name = "durationPerEpisode", value = "单集时长（分钟）")
	private BigDecimal durationPerEpisode;

	@ApiModelProperty(name = "episodes", value = "集数")
	private Integer episodes;

	@ApiModelProperty(name = "status", value = "征稿状态文案：征稿中/即将截止/已结束")
	private String status;

	@ApiModelProperty(name = "joined", value = "当前登录创作者是否已参与")
	private Boolean joined;
}
