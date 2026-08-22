package com.playlet.oversea.query.drama;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DramaAssetAuditHandleQuery {

	@NotNull(message = "剧集ID不能为空")
	@ApiModelProperty(name = "assetId", value = "drama_asset.id", required = true, dataType = "Integer")
	private Integer assetId;

	@NotNull(message = "审核组不能为空")
	@ApiModelProperty(name = "stepType", value = "审核组：2=A组 3=B组", required = true, dataType = "Integer")
	private Integer stepType;

	@NotNull(message = "审核动作不能为空")
	@ApiModelProperty(name = "action", value = "动作：1通过 2驳回", required = true, dataType = "Integer")
	private Integer action;

	@ApiModelProperty(name = "remark", value = "备注", required = false, dataType = "String")
	private String remark;
}
