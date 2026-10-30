package com.playlet.internal.service.impl;

import com.playlet.internal.api.response.AuditModuleStatRespEntity;
import com.playlet.internal.api.response.AuditSummaryRespEntity;
import com.playlet.internal.api.response.AuditUrgentTodoRespEntity;
import com.playlet.internal.api.response.DramaCategoryAuditStatRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.drama.AuditSummaryDao;
import com.playlet.internal.service.AuditSummaryManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 审核汇总实现：只做聚合，不落库；数据全部实时统计。
 */
@Slf4j
@RestController
@CrossOrigin
public class AuditSummaryManageServiceImpl extends BaseApiService implements AuditSummaryManageService {

	/** 剧评审 / 剧集评审的超时阈值（小时） */
	private static final int SLA_HOURS = 12;

	/** 紧急待办展示条数 */
	private static final int TODO_LIMIT = 8;

	/** 敏感词模块：词库量/命中数不在「累计通过/今日完成」口径内 */
	private static final String MODULE_WORD = "SENSITIVE_WORD";

	@Autowired
	private AuditSummaryDao auditSummaryDao;

	@Override
	public ResponseBase summary() {
		try {
			List<AuditModuleStatRespEntity> modules = auditSummaryDao.findModuleStats(SLA_HOURS);
			List<DramaCategoryAuditStatRespEntity> categories = auditSummaryDao.findCategoryStats();
			List<AuditUrgentTodoRespEntity> todos = auditSummaryDao.findUrgentTodos(TODO_LIMIT);

			AuditSummaryRespEntity resp = new AuditSummaryRespEntity();
			fillModules(modules);
			Map<String, AuditModuleStatRespEntity> byModule = indexModules(modules);

			resp.setModules(modules);
			resp.setCategories(fillCategories(categories));
			resp.setUrgentTodos(markTodos(todos));

			// 顶部指标：敏感词模块的词库量不参与审核通过/完成口径
			int pendingTotal = 0, todayDone = 0, passedTotal = 0, rejectedTotal = 0, overdueTotal = 0;
			double avgSum = 0;
			int avgCount = 0;
			for (AuditModuleStatRespEntity item : modules) {
				pendingTotal += nullToZero(item.getPendingCount());
				overdueTotal += nullToZero(item.getOverdueCount());
				if (MODULE_WORD.equals(item.getModule())) {
					continue;
				}
				todayDone += nullToZero(item.getTodayCount());
				passedTotal += nullToZero(item.getPassedCount());
				rejectedTotal += nullToZero(item.getRejectedCount());
				if (item.getAvgHours() != null) {
					avgSum += item.getAvgHours();
					avgCount++;
				}
			}
			resp.setPendingTotal(pendingTotal);
			resp.setTodayDone(todayDone);
			resp.setPassedTotal(passedTotal);
			resp.setRejectedTotal(rejectedTotal);
			resp.setOverdueTotal(overdueTotal);
			resp.setAvgHandleHours(avgCount > 0 ? round1(avgSum / avgCount) : 0D);
			resp.setPendingBreakdown(buildBreakdown(byModule));
			return setResultSuccess(resp, "查询成功");
		} catch (Exception e) {
			log.error("audit summary failed", e);
			return setResultError("审核汇总统计失败：" + e.getMessage());
		}
	}

	/** 计算各模块通过率 */
	private void fillModules(List<AuditModuleStatRespEntity> modules) {
		if (modules == null) {
			return;
		}
		for (AuditModuleStatRespEntity item : modules) {
			int passed = nullToZero(item.getPassedCount());
			int rejected = nullToZero(item.getRejectedCount());
			item.setPassRate(passed + rejected > 0 ? round1(passed * 100.0 / (passed + rejected)) : 0D);
		}
	}

	private Map<String, AuditModuleStatRespEntity> indexModules(List<AuditModuleStatRespEntity> modules) {
		Map<String, AuditModuleStatRespEntity> map = new LinkedHashMap<>();
		if (modules == null) {
			return map;
		}
		for (AuditModuleStatRespEntity item : modules) {
			map.put(item.getModule(), item);
		}
		return map;
	}

	/** 待审分布文案：信息 / 剧集 / 备案 / 评论 */
	private String buildBreakdown(Map<String, AuditModuleStatRespEntity> byModule) {
		return String.format("信息 %d / 剧集 %d / 备案 %d / 评论 %d",
				pendingOf(byModule, "DRAMA_INFO"),
				pendingOf(byModule, "DRAMA_EPISODE"),
				pendingOf(byModule, "DRAMA_RECORD"),
				pendingOf(byModule, "COMMENT"));
	}

	private int pendingOf(Map<String, AuditModuleStatRespEntity> byModule, String module) {
		AuditModuleStatRespEntity item = byModule.get(module);
		return item == null ? 0 : nullToZero(item.getPendingCount());
	}

	/** 分类统计补齐一类/二类/三类，避免前端空卡片 */
	private List<DramaCategoryAuditStatRespEntity> fillCategories(List<DramaCategoryAuditStatRespEntity> rows) {
		Map<Integer, DramaCategoryAuditStatRespEntity> map = new LinkedHashMap<>();
		if (rows != null) {
			for (DramaCategoryAuditStatRespEntity row : rows) {
				map.put(row.getCategory(), row);
			}
		}
		List<DramaCategoryAuditStatRespEntity> result = new ArrayList<>();
		for (int category = 1; category <= 3; category++) {
			DramaCategoryAuditStatRespEntity item = map.get(category);
			if (item == null) {
				item = new DramaCategoryAuditStatRespEntity();
				item.setCategory(category);
				item.setTotalCount(0);
				item.setPassedCount(0);
				item.setPassRate(0D);
				item.setAvgHours(0D);
			}
			item.setCategoryLabel(categoryLabel(category));
			int total = nullToZero(item.getTotalCount());
			int passed = nullToZero(item.getPassedCount());
			item.setPassRate(total > 0 ? round1(passed * 100.0 / total) : 0D);
			result.add(item);
		}
		return result;
	}

	private String categoryLabel(Integer category) {
		if (category == null) {
			return "未分类";
		}
		switch (category) {
			case 1:
				return "一类短剧";
			case 2:
				return "二类短剧";
			case 3:
				return "三类短剧";
			default:
				return "未分类";
		}
	}

	/** 标记超时与标签样式：超时=红，高危评论/复审=橙，其余=蓝 */
	private List<AuditUrgentTodoRespEntity> markTodos(List<AuditUrgentTodoRespEntity> todos) {
		if (todos == null) {
			return new ArrayList<>();
		}
		for (AuditUrgentTodoRespEntity item : todos) {
			int remain = item.getRemainMinutes() == null ? 0 : item.getRemainMinutes();
			item.setOverdue(remain <= 0);
			item.setTagType(tagType(item, remain));
		}
		return todos;
	}

	private String tagType(AuditUrgentTodoRespEntity item, int remainMinutes) {
		if (remainMinutes <= 0) {
			return "danger";
		}
		String tag = item.getTagLabel() == null ? "" : item.getTagLabel();
		if (tag.contains("高危")) {
			return "danger";
		}
		if (tag.contains("复审")) {
			return "warning";
		}
		return "info";
	}

	private int nullToZero(Integer value) {
		return value == null ? 0 : value;
	}

	private Double round1(double value) {
		return Math.round(value * 10.0) / 10.0;
	}
}
