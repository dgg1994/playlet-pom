package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 集批量上架/下架失败项。
 */
@Data
@ApiModel(value = "集批量上下架失败项", description = "单集失败原因")
public class DramaAssetBatchShelfFailItem {

	@ApiModelProperty("集ID")
	private Integer assetId;

	@ApiModelProperty("失败原因文案")
	private String message;
}
