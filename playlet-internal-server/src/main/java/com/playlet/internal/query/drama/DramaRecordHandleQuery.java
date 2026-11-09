package com.playlet.internal.query.drama;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 备案审核处理（通过/驳回）。
 */
@Data
public class DramaRecordHandleQuery {

	@NotNull(message = "短剧ID不能为空")
	@ApiModelProperty(name = "dramaId", value = "drama.id", required = true, dataType = "Integer")
	private Integer dramaId;

	@NotNull(message = "审核步骤类型不能为空")
	@ApiModelProperty(name = "stepType", value = "审核步骤类型：1初审 2复审", required = true, dataType = "Integer")
	private Integer stepType;

	@NotNull(message = "审核动作不能为空")
	@ApiModelProperty(name = "action", value = "动作：1通过 2驳回", required = true, dataType = "Integer")
	private Integer action;

	@ApiModelProperty(name = "remark", value = "审核意见/驳回原因（驳回时必填）", dataType = "String")
	private String remark;

	@ApiModelProperty(name = "filingCategory",
			value = "备案分类判定：1一类（官方备案）2二类（官方备案）3三类（平台备案）；通过前必须判定",
			dataType = "Integer")
	private Integer filingCategory;

	@ApiModelProperty(name = "unlockPrice", value = "剧集解锁金额（元），审核时可修改", dataType = "BigDecimal")
	private java.math.BigDecimal unlockPrice;
}
