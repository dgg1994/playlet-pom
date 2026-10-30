package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 审核汇总：管理端「审核管理 → 审核汇总」页数据。
 * 网关：/china/admin/auditSummary/**
 */
@RequestMapping("/auditSummary")
@Api(value = "审核汇总", tags = "审核汇总")
public interface AuditSummaryManageService {

	@PostMapping("/summary")
	@ApiOperation(value = "审核汇总", notes = "顶部指标（待审/今日完成/累计通过/累计驳回/平均时长/超时）+ 各模块概览 + 备案分类统计 + 紧急待办 TOP N")
	ResponseBase summary();
}
