package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 人才市场列表卡片。
 */
@Data
@ApiModel(value = "人才市场卡片")
public class TalentMarketCardRespEntity {

	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@ApiModelProperty(name = "marketRole", value = "市场角色 1编剧 2创作者")
	private Integer marketRole;

	@ApiModelProperty(name = "name", value = "展示名称")
	private String name;

	@ApiModelProperty(name = "avatarUrl", value = "头像签名 URL，可空")
	private String avatarUrl;

	@ApiModelProperty(name = "type", value = "资质类型文案：个人/公司/团队")
	private String type;

	@ApiModelProperty(name = "badge", value = "徽章")
	private String badge;

	@ApiModelProperty(name = "certified", value = "是否已认证")
	private Boolean certified;

	@ApiModelProperty(name = "production", value = "制作类型（创作者）")
	private String production;

	@ApiModelProperty(name = "skills", value = "技能")
	private List<String> skills;

	@ApiModelProperty(name = "tags", value = "擅长题材/标签")
	private List<String> tags;

	@ApiModelProperty(name = "services", value = "承接服务")
	private List<String> services;

	@ApiModelProperty(name = "style", value = "技术风格")
	private String style;

	@ApiModelProperty(name = "score", value = "综合评分")
	private BigDecimal score;

	@ApiModelProperty(name = "city", value = "所在地")
	private String city;

	@ApiModelProperty(name = "works", value = "作品数")
	private Integer works;

	@ApiModelProperty(name = "deals", value = "成交单数")
	private Integer deals;

	@ApiModelProperty(name = "revenue", value = "累计收益（元）")
	private Long revenue;

	@ApiModelProperty(name = "price", value = "起价（元）")
	private BigDecimal price;

	@ApiModelProperty(name = "priceUnit", value = "计价单位 部/集")
	private String priceUnit;

	@ApiModelProperty(name = "status", value = "接单状态文案：可接单/排期中")
	private String status;
}
