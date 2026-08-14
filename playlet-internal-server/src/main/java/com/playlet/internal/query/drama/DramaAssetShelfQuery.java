package com.playlet.internal.query.drama;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DramaAssetShelfQuery {

	@NotNull(message = "assetId不能为空")
	@ApiModelProperty(name = "assetId", value = "集ID", required = true, dataType = "Integer")
	private Integer assetId;
}
