package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 看剧喜好保存入参（按分类全量覆盖）。
 */
@Data
@ApiModel(value = "看剧喜好保存请求", description = "按分类提交已选 preferGroupIds，后端按 uid 全量覆盖")
public class WatchPreferSaveRequest {

	@ApiModelProperty(value = "各分类已选偏好", required = true)
	private List<WatchPreferSaveItemRequest> items;
}
