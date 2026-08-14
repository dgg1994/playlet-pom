package com.playlet.internal.service;

public interface DramaAssetAuditService {

	/**
	 * 初始化审核步骤（AI默认通过，A/B待审）。
	 */
	void initAuditStepsOnRelease(Integer assetId, Integer dramaId);

	/**
	 * 按步骤重算聚合审核状态；过审不自动上架，驳回/待审强制下架并 sync 剧。
	 */
	void refreshAggregateAndAutoShelf(Integer assetId);
}
