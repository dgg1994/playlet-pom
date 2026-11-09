package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 剧本市场筛选项与排序字典。
 */
@Data
@ApiModel(value = "剧本市场筛选项", description = "筛选分组 + 排序页签")
public class ScriptFilterDictRespEntity {

	@ApiModelProperty("筛选分组")
	private List<FilterGroup> filterGroups;

	@ApiModelProperty("排序页签")
	private List<SortTab> sortTabs;

	@Data
	@ApiModel(value = "筛选分组")
	public static class FilterGroup {
		private String key;
		private String label;
		private List<Option> options;
	}

	@Data
	@ApiModel(value = "筛选项")
	public static class Option {
		private String label;
		private Object value;
	}

	@Data
	@ApiModel(value = "排序页签")
	public static class SortTab {
		private String label;
		private Object value;
	}
}
