package com.playlet.oversea.query.drama;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 集上架/下架请求。
 */
@Data
@ApiModel(value = "集上架请求", description = "创作者集上架/下架入参")
public class DramaAssetShelfQuery {

	@NotNull(message = "assetId不能为空")
	@ApiModelProperty(name = "assetId", value = "集ID", required = true, dataType = "Integer")
	private Integer assetId;

	@NotNull(message = "shelfStatus不能为空")
	@ApiModelProperty(name = "shelfStatus", value = "上架状态 0下架 1上架", required = true, dataType = "Integer")
	private Integer shelfStatus;
}
