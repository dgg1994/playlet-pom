package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创作者代表作：新增 / 修改 / 删除入参。
 */
@Data
@ApiModel(value = "创作者代表作入参")
public class CreatorRepresentativeSaveQuery {

	@ApiModelProperty(value = "主键（有值=修改，无值=新增）")
	private Integer id;

	@ApiModelProperty(value = "短剧名称")
	private String dramaName;

	@ApiModelProperty(value = "备案分类")
	private String category;

	@ApiModelProperty(value = "封面图片key（选填）")
	private String cover;

	@ApiModelProperty(value = "作品链接（必填）")
	private String workLink;

	@ApiModelProperty(value = "简介")
	private String intro;
}
