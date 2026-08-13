package com.playlet.internal.service;

/**
 * 剧级评审：封面/简介/标签，AI 默认通过，A/B 并行，全过自动上架。
 */
public interface DramaAuditService {

	/**
	 * 初始化或重置审核步骤（AI 默认通过，A/B 待审）。
	 */
	void initAuditSteps(Integer dramaId);

	/**
	 * 按步骤重算聚合状态；全过时自动上架（sync verify_status）。
	 */
	void refreshAggregateAndAutoShelf(Integer dramaId);
}
