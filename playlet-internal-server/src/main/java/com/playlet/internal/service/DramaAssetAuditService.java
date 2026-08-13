package com.playlet.internal.service;

public interface DramaAssetAuditService {

	/**
	 * 初始化审核步骤（AI默认通过，A/B待审）。
	 */
	void initAuditStepsOnRelease(Integer assetId, Integer dramaId);

	/**
	 * 按步骤重算聚合状态；满足条件时自动上架。
	 */
	void refreshAggregateAndAutoShelf(Integer assetId);
}
