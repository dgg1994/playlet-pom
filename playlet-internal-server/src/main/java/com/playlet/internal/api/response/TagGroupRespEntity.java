package com.playlet.internal.api.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 标签分组列表行：固定字段 + 语言 code 平铺（如 zh-cn / en）
 */
@Data
@ApiModel("短剧标签分组行")
public class TagGroupRespEntity {

	@ApiModelProperty("分组id")
	private String groupId;

	@ApiModelProperty("排序权重，越大越靠前")
	private Integer sortWeight;

	@ApiModelProperty("1启用0停用")
	private Integer status;

	@ApiModelProperty("创建时间")
	private Date setTime;

	@ApiModelProperty("更新时间")
	private Date gmtModified;

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	private Map<String, String> langNames = new LinkedHashMap<>();

	@JsonAnyGetter
	public Map<String, String> anyLangNames() {
		return langNames;
	}

	public void putLangName(String langue, String tagName) {
		if (langue == null) {
			return;
		}
		langNames.put(langue, tagName);
	}
}
