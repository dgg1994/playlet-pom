package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 看剧喜好保存-单分类项。
 */
@Data
@ApiModel(value = "看剧喜好保存分类项", description = "某一分类下用户已选的偏好 groupId 列表")
public class WatchPreferSaveItemRequest {

	@ApiModelProperty(value = "分类编码：CHANNEL/ERA/STYLE/THEME/ROLE", required = true, example = "CHANNEL")
	private String categoryCode;

	@ApiModelProperty(value = "该分类下已选选项 groupId 列表；未选传空数组", required = true,
			example = "[\"wp_channel_male\"]")
	private List<String> preferGroupIds;
}
