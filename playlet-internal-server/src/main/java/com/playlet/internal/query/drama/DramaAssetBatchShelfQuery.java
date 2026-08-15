package com.playlet.internal.query.drama;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 集批量上架/下架请求。
 */
@Data
@ApiModel(value = "集批量上架下架", description = "assetIds 批量操作入参")
public class DramaAssetBatchShelfQuery {

	@NotEmpty(message = "assetIds不能为空")
	@ApiModelProperty(name = "assetIds", value = "集ID列表", required = true)
	private List<Integer> assetIds;
}
