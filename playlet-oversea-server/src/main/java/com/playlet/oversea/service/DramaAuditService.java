package com.playlet.oversea.service;

/**
 * 剧级评审：封面/简介/标签，AI 默认通过，A/B 并行。
 * 过审 ≠ 上架；剧上架由「已上架集」推导（见 {@link #syncDramaShelfByEpisodes}）。
 */
public interface DramaAuditService {

	/**
	 * 初始化或重置审核步骤（AI 默认通过，A/B 待审）。
	 */
	void initAuditSteps(Integer dramaId);

	/**
	 * 按步骤重算聚合审核状态；过审不自动上架，驳回/待审强制下架。
	 */
	void refreshAggregateAndAutoShelf(Integer dramaId);

	/**
	 * 按集上架情况同步剧可见性：
	 * 有已上架集且剧已过审 → 剧上架；否则剧下架。
	 */
	void syncDramaShelfByEpisodes(Integer dramaId);

	/**
	 * 强制下架整剧及其所有已上架集，再按集同步（用于剧驳回等）。
	 */
	void forceUnshelfDramaAndEpisodes(Integer dramaId);

	/**
	 * 管理端整剧上架：批量上架已过审集，再按集同步剧可见性（与 {@link #forceUnshelfDramaAndEpisodes} 对称）。
	 */
	void forceShelfDramaAndApprovedEpisodes(Integer dramaId);
}
