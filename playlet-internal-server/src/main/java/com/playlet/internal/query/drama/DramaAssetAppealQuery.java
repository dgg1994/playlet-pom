package com.playlet.internal.query.drama;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 集评审申诉请求。
 */
@Data
@ApiModel(value = "集申诉请求", description = "驳回后作者提起集申诉")
public class DramaAssetAppealQuery {

	@NotNull(message = "assetId不能为空")
	@ApiModelProperty(name = "assetId", value = "集ID", required = true, dataType = "Integer")
	private Integer assetId;

	@NotBlank(message = "申诉理由不能为空")
	@ApiModelProperty(name = "remark", value = "申诉理由", required = true, dataType = "String")
	private String remark;
}
