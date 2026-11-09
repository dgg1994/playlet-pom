package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 剧本市场详情。
 */
@Data
@ApiModel(value = "剧本详情", description = "市场详情 / 作家端详情共用")
public class ScriptMarketDetailRespEntity {

	@ApiModelProperty("剧本ID")
	private Integer id;
	private String title;
	private String coverUrl;
	private Integer channel;
	private String channelText;
	private String topic;
	private Integer licenseType;
	private String licenseText;
	private String licenseScope;
	private Integer type;
	private String typeText;
	private Integer finishState;
	private String finishStateText;
	private Integer episodes;
	private BigDecimal avgMinutes;
	private BigDecimal price;
	private Integer saleStatus;
	private String saleStatusText;
	private BigDecimal score;
	private Long heat;
	private Long views;
	private Long favorites;
	private String summary;
	private Date updatedAt;
	private List<String> tags;
	private Core core;
	private List<Character> characters;
	private List<Episode> episodeList;
	private Preview preview;
	private ScriptAuthorRespEntity author;
	private Boolean favorited;
	private MyApply myApply;

	@Data
	@ApiModel(value = "故事核心")
	public static class Core {
		private String corePoint;
		private String oneLine;
		private String topics;
		private String benchmarks;
	}

	@Data
	@ApiModel(value = "人物")
	public static class Character {
		private String name;
		private Integer role;
		private String roleText;
		private String avatarUrl;
		private String description;
	}

	@Data
	@ApiModel(value = "分集")
	public static class Episode {
		private Integer no;
		private String title;
		private Integer durationSeconds;
		private String durationText;
	}

	@Data
	@ApiModel(value = "试读")
	public static class Preview {
		private Integer episodeNo;
		private String episodeTitle;
		private List<Scene> scenes;
	}

	@Data
	@ApiModel(value = "试读场景")
	public static class Scene {
		private String sceneTitle;
		private List<Line> lines;
	}

	@Data
	@ApiModel(value = "试读台词")
	public static class Line {
		private String role;
		private String text;
	}

	@Data
	@ApiModel(value = "我的申请")
	public static class MyApply {
		private Long applyId;
		private Integer applyType;
		private Integer applyStatus;
		private String applyStatusText;
	}
}
