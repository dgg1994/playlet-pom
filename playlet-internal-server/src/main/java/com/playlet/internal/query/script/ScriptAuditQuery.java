package com.playlet.internal.query.script;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理端剧本审核入参。
 */
@Data
@ApiModel(value = "剧本审核", description = "通过或驳回")
public class ScriptAuditQuery {

	@NotNull(message = "id 不能为空")
	@ApiModelProperty(name = "id", value = "剧本ID", required = true)
	private Integer id;

	@NotNull(message = "auditStatus 不能为空")
	@ApiModelProperty(name = "auditStatus", value = "2通过 3驳回", required = true)
	private Integer auditStatus;

	@ApiModelProperty(name = "rejectReason", value = "驳回原因，驳回必填")
	private String rejectReason;
}
