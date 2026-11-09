package com.playlet.internal.api.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 剧本市场列表卡片。
 */
@Data
@ApiModel(value = "剧本市场卡片", description = "列表行")
public class ScriptMarketCardRespEntity {

	@ApiModelProperty("剧本ID")
	private Integer id;

	@ApiModelProperty("剧本名称")
	private String title;

	@ApiModelProperty("封面签名URL")
	private String coverUrl;

	@ApiModelProperty("内容分区")
	private Integer channel;

	@ApiModelProperty("内容分区文案")
	private String channelText;

	@ApiModelProperty("主题材")
	private String topic;

	@ApiModelProperty("授权类型")
	private Integer licenseType;

	@ApiModelProperty("授权类型文案")
	private String licenseText;

	@ApiModelProperty("集数")
	private Integer episodes;

	@ApiModelProperty("单集平均时长（分钟）")
	private BigDecimal avgMinutes;

	@ApiModelProperty("售价（元）")
	private BigDecimal price;

	@ApiModelProperty("售卖状态")
	private Integer saleStatus;

	@ApiModelProperty("售卖状态文案")
	private String saleStatusText;

	@ApiModelProperty("评分")
	private BigDecimal score;

	@ApiModelProperty("热度值")
	private Long heat;

	@ApiModelProperty("简介")
	private String summary;

	@ApiModelProperty("标签")
	private List<String> tags;

	@ApiModelProperty("编剧信息")
	private ScriptAuthorRespEntity author;

	@ApiModelProperty("发布时间")
	private Date publishedAt;

	@JsonIgnore
	private Integer authorId;
	@JsonIgnore
	private String authorName;
	@JsonIgnore
	private String authorAvatar;
	@JsonIgnore
	private Integer authorScriptCount;
	@JsonIgnore
	private BigDecimal authorScore;
}
