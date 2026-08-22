package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创作者首页近期热点题材。
 */
@Data
@ApiModel(value = "创作者首页热点题材", description = "标签气泡")
public class CreatorHomeHotTagRespEntity {

	@ApiModelProperty("标签分组ID")
	private String groupId;

	@ApiModelProperty("标签名")
	private String tagName;

	@ApiModelProperty("是否打火（近期较热）1是0否")
	private Integer hotFlag;
}
