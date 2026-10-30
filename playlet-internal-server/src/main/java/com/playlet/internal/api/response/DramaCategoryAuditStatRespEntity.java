package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 审核汇总：按短剧备案分类（一类/二类/三类）的审核统计
 */
@Data
@ApiModel("审核汇总-分类统计")
public class DramaCategoryAuditStatRespEntity {

	@ApiModelProperty("备案分类：1一类 2二类 3三类")
	private Integer category;

	@ApiModelProperty("分类名称，如 一类短剧")
	private String categoryLabel;

	@ApiModelProperty("总提交数")
	private Integer totalCount;

	@ApiModelProperty("审核通过数")
	private Integer passedCount;

	@ApiModelProperty("通过率（0~100，保留1位小数）")
	private Double passRate;

	@ApiModelProperty("平均审核时长（小时）")
	private Double avgHours;
}
