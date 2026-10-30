package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 审核汇总页数据：顶部指标 + 各模块概览 + 备案分类统计 + 紧急待办
 */
@Data
@ApiModel("审核汇总")
public class AuditSummaryRespEntity {

	@ApiModelProperty("待审核总数")
	private Integer pendingTotal;

	@ApiModelProperty("今日完成数")
	private Integer todayDone;

	@ApiModelProperty("累计通过数")
	private Integer passedTotal;

	@ApiModelProperty("累计驳回数")
	private Integer rejectedTotal;

	@ApiModelProperty("平均处理时长（小时）")
	private Double avgHandleHours;

	@ApiModelProperty("超时未处理数")
	private Integer overdueTotal;

	@ApiModelProperty("待审分布文案，如 信息 6 / 剧集 8 / 备案 5 / 评论 9")
	private String pendingBreakdown;

	@ApiModelProperty("各模块审核概览")
	private List<AuditModuleStatRespEntity> modules;

	@ApiModelProperty("按短剧分类审核统计")
	private List<DramaCategoryAuditStatRespEntity> categories;

	@ApiModelProperty("今日紧急待办 TOP N")
	private List<AuditUrgentTodoRespEntity> urgentTodos;
}
