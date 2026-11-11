package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 人才市场详情。
 */
@Data
@ApiModel(value = "人才市场详情")
public class TalentMarketDetailRespEntity {

	@ApiModelProperty(name = "kind", value = "writer 编剧 / creator 创作者")
	private String kind;

	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@ApiModelProperty(name = "name", value = "展示名称")
	private String name;

	@ApiModelProperty(name = "avatarUrl", value = "头像签名 URL，可空")
	private String avatarUrl;

	@ApiModelProperty(name = "type", value = "资质类型文案")
	private String type;

	@ApiModelProperty(name = "certLabel", value = "认证徽章文案")
	private String certLabel;

	@ApiModelProperty(name = "tags", value = "标签 chips")
	private List<String> tags;

	@ApiModelProperty(name = "stats", value = "顶部统计 4 项")
	private List<StatItem> stats;

	@ApiModelProperty(name = "intro", value = "简介")
	private String intro;

	@ApiModelProperty(name = "sideRows", value = "右侧栏信息行")
	private List<SideRow> sideRows;

	@ApiModelProperty(name = "sections", value = "内容区块：list/cards/packages")
	private List<Section> sections;

	/**
	 * 统计项。
	 */
	@Data
	@ApiModel(value = "统计项")
	public static class StatItem {
		@ApiModelProperty(name = "label", value = "名称")
		private String label;
		@ApiModelProperty(name = "value", value = "值")
		private String value;
		@ApiModelProperty(name = "suffix", value = "后缀")
		private String suffix;
		@ApiModelProperty(name = "tone", value = "色调：orange/red/green")
		private String tone;
	}

	/**
	 * 侧栏信息行。
	 */
	@Data
	@ApiModel(value = "侧栏信息行")
	public static class SideRow {
		@ApiModelProperty(name = "label", value = "名称")
		private String label;
		@ApiModelProperty(name = "value", value = "值")
		private String value;
		@ApiModelProperty(name = "tone", value = "色调")
		private String tone;
	}

	/**
	 * 内容区块。items 结构随 kind 不同：
	 * list={title,meta,rightLabel,rightValue,highlight}；
	 * cards={title,tags,meta}；packages={title,desc,price,priceUnit}。
	 */
	@Data
	@ApiModel(value = "内容区块")
	public static class Section {
		@ApiModelProperty(name = "title", value = "区块标题")
		private String title;
		@ApiModelProperty(name = "kind", value = "list/cards/packages")
		private String kind;
		@ApiModelProperty(name = "items", value = "区块条目")
		private List<Map<String, Object>> items;
	}
}
