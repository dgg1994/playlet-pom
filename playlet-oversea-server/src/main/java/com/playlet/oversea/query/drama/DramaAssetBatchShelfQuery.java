package com.playlet.oversea.query.drama;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 集批量上架/下架请求。
 */
@Data
@ApiModel(value = "集批量上架下架", description = "assetIds + shelfStatus 批量操作入参")
public class DramaAssetBatchShelfQuery {

	@NotEmpty(message = "assetIds不能为空")
	@ApiModelProperty(name = "assetIds", value = "集ID列表", required = true)
	private List<Integer> assetIds;

	@NotNull(message = "shelfStatus不能为空")
	@ApiModelProperty(name = "shelfStatus", value = "上架状态 0下架 1上架", required = true, dataType = "Integer")
	private Integer shelfStatus;
}
