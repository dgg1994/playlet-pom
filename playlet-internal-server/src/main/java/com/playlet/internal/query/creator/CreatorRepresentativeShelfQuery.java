package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创作者代表作上架/下架入参。
 */
@Data
@ApiModel(value = "创作者代表作上下架入参")
public class CreatorRepresentativeShelfQuery {

	@NotNull(message = "id不能为空")
	@ApiModelProperty(value = "代表作主键", required = true)
	private Integer id;

	@NotNull(message = "shelfStatus不能为空")
	@ApiModelProperty(value = "上架状态 0下架 1上架", required = true)
	private Integer shelfStatus;
}
