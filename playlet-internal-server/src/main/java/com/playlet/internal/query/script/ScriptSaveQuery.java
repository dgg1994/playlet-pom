package com.playlet.internal.query.script;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * 作家端新增/编辑剧本入参。
 */
@Data
@ApiModel(value = "剧本保存", description = "新增或编辑，人物/分集/试读全量覆盖")
public class ScriptSaveQuery {

	@ApiModelProperty(name = "id", value = "剧本ID，空=新增")
	private Integer id;

	@NotBlank(message = "title 不能为空")
	@Size(max = 128, message = "title 过长")
	@ApiModelProperty(name = "title", value = "剧本名称", required = true)
	private String title;

	@ApiModelProperty(name = "coverKey", value = "封面七牛 key")
	private String coverKey;

	@NotNull(message = "channel 不能为空")
	@ApiModelProperty(name = "channel", value = "内容分区 1女频 2男频 3合家欢", required = true)
	private Integer channel;

	@ApiModelProperty(name = "topic", value = "主题材")
	private String topic;

	@ApiModelProperty(name = "licenseType", value = "授权类型 1独家 2普通 3可改编")
	private Integer licenseType;

	@ApiModelProperty(name = "licenseScope", value = "授权范围描述")
	private String licenseScope;

	@ApiModelProperty(name = "episodes", value = "集数")
	private Integer episodes;

	@ApiModelProperty(name = "avgMinutes", value = "单集平均时长（分钟）")
	private BigDecimal avgMinutes;

	@ApiModelProperty(name = "price", value = "售价（元）")
	private BigDecimal price;

	@ApiModelProperty(name = "scriptType", value = "剧本类型 1真人 2AI漫剧")
	private Integer scriptType;

	@ApiModelProperty(name = "finishState", value = "完成状态 0连载 1完结")
	private Integer finishState;

	@ApiModelProperty(name = "summary", value = "简介")
	private String summary;

	@ApiModelProperty(name = "corePoint", value = "核心看点")
	private String corePoint;

	@ApiModelProperty(name = "oneLine", value = "一句话故事")
	private String oneLine;

	@ApiModelProperty(name = "benchmarkWorks", value = "对标作品")
	private String benchmarkWorks;

	@ApiModelProperty(name = "attachmentKey", value = "剧本全文附件 key")
	private String attachmentKey;

	@ApiModelProperty(name = "submit", value = "0草稿 1提交待审，默认1")
	private Integer submit;

	@ApiModelProperty(name = "tags", value = "标签名称集合")
	private List<String> tags;

	@ApiModelProperty(name = "characters", value = "人物介绍")
	private List<CharacterItem> characters;

	@ApiModelProperty(name = "episodeList", value = "分集结构")
	private List<EpisodeItem> episodeList;

	@ApiModelProperty(name = "previews", value = "试读场景")
	private List<PreviewItem> previews;

	@Data
	@ApiModel(value = "剧本人物入参")
	public static class CharacterItem {
		@ApiModelProperty(name = "name", value = "人物姓名")
		private String name;
		@ApiModelProperty(name = "role", value = "1男主 2女主 3男二 4女二 9其他")
		private Integer role;
		@ApiModelProperty(name = "avatarUrl", value = "头像七牛 key")
		private String avatarUrl;
		@ApiModelProperty(name = "description", value = "人物介绍")
		private String description;
	}

	@Data
	@ApiModel(value = "剧本分集入参")
	public static class EpisodeItem {
		@ApiModelProperty(name = "no", value = "集号")
		private Integer no;
		@ApiModelProperty(name = "title", value = "分集标题")
		private String title;
		@ApiModelProperty(name = "durationSeconds", value = "预估时长（秒）")
		private Integer durationSeconds;
	}

	@Data
	@ApiModel(value = "剧本试读入参")
	public static class PreviewItem {
		@ApiModelProperty(name = "episodeNo", value = "集号")
		private Integer episodeNo;
		@ApiModelProperty(name = "sceneNo", value = "场景序号")
		private Integer sceneNo;
		@ApiModelProperty(name = "sceneTitle", value = "场景标题")
		private String sceneTitle;
		@ApiModelProperty(name = "lines", value = "台词行")
		private List<LineItem> lines;
	}

	@Data
	@ApiModel(value = "剧本台词入参")
	public static class LineItem {
		@ApiModelProperty(name = "role", value = "角色名，空为旁白")
		private String role;
		@ApiModelProperty(name = "text", value = "台词/动作")
		private String text;
	}
}
