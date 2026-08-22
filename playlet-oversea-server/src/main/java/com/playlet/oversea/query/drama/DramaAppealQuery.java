package com.playlet.oversea.query.drama;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 剧评审申诉请求。
 */
@Data
@ApiModel(value = "剧申诉请求", description = "驳回后作者提起剧申诉")
public class DramaAppealQuery {

	@NotNull(message = "dramaId不能为空")
	@ApiModelProperty(name = "dramaId", value = "剧ID", required = true, dataType = "Integer")
	private Integer dramaId;

	@NotBlank(message = "申诉理由不能为空")
	@ApiModelProperty(name = "remark", value = "申诉理由", required = true, dataType = "String")
	private String remark;
}
