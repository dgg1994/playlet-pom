package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 征稿广场详情。
 */
@Data
@ApiModel(value = "征稿详情")
public class CallDetailRespEntity {

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

	@ApiModelProperty(name = "tags", value = "标签 chips")
	private List<String> tags;

	@ApiModelProperty(name = "desc", value = "摘要")
	private String desc;

	@ApiModelProperty(name = "benchmark", value = "对标作品")
	private String benchmark;

	@ApiModelProperty(name = "durationPerEpisode", value = "单集时长（分钟）")
	private BigDecimal durationPerEpisode;

	@ApiModelProperty(name = "episodes", value = "集数")
	private Integer episodes;

	@ApiModelProperty(name = "status", value = "征稿状态文案")
	private String status;

	@ApiModelProperty(name = "daysLeft", value = "剩余天数（已结束为 0）")
	private Integer daysLeft;

	@ApiModelProperty(name = "publisher", value = "征稿方")
	private String publisher;

	@ApiModelProperty(name = "publishAt", value = "发布时间 yyyy-MM-dd")
	private String publishAt;

	@ApiModelProperty(name = "selectionDays", value = "选稿周期")
	private String selectionDays;

	@ApiModelProperty(name = "settle", value = "结算方式")
	private String settle;

	@ApiModelProperty(name = "licenseMode", value = "授权方式：独家授权/非独家授权")
	private String licenseMode;

	@ApiModelProperty(name = "licenseNote", value = "授权说明")
	private String licenseNote;

	@ApiModelProperty(name = "wordCount", value = "征稿字数说明")
	private String wordCount;

	@ApiModelProperty(name = "episodeFormat", value = "单集时长与集数说明")
	private String episodeFormat;

	@ApiModelProperty(name = "contentRequirement", value = "内容要求")
	private String contentRequirement;

	@ApiModelProperty(name = "leadCast", value = "主演资料")
	private String leadCast;

	@ApiModelProperty(name = "supportCast", value = "助演资料")
	private String supportCast;

	@ApiModelProperty(name = "benchmarkNote", value = "参考对标详情")
	private String benchmarkNote;

	@ApiModelProperty(name = "extraNote", value = "其他补充")
	private String extraNote;

	@ApiModelProperty(name = "attachments", value = "相关附件 [{name}]")
	private List<Map<String, Object>> attachments;

	@ApiModelProperty(name = "submissions", value = "已投稿剧本 [{title,user,time,intro,attachments}]")
	private List<CallSubmissionRespEntity> submissions;

	@ApiModelProperty(name = "related", value = "相关征稿 [{id,title,need,amount,deadlineText}]")
	private List<Map<String, Object>> related;

	@ApiModelProperty(name = "joined", value = "当前登录创作者是否已参与")
	private Boolean joined;

	@ApiModelProperty(name = "submitted", value = "当前登录创作者是否已投稿")
	private Boolean submitted;

	@ApiModelProperty(name = "favorited", value = "当前登录创作者是否已收藏")
	private Boolean favorited;
}
