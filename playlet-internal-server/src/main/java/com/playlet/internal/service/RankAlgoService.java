package com.playlet.internal.service;

/**
 * 算法榜刷新：热播 / 新剧 / 飙升 / 推荐 / 热搜 / 收藏。
 * 各方法算法细节见 {@link com.playlet.internal.service.impl.RankAlgoServiceImpl}。
 */
public interface RankAlgoService {

	/** 刷新全部算法榜 */
	void refreshAllP0();

	/**
	 * 热播：近7日 valid×0.6 + collect×0.2 + like×0.1 + play×0.1
	 */
	void refreshHotPlayBoard();

	/**
	 * 新剧：上架14天内，公式同热播
	 */
	void refreshNewBoard();

	/**
	 * 飙升：近3日 vs 前3日，(Δvalid)×0.7 + (Δplay)×0.3
	 */
	void refreshRisingBoard();

	/**
	 * 推荐：近7日综合分 + 30天新鲜度加分
	 */
	void refreshRecommendBoard();

	/**
	 * 热搜：近7日 search_cnt 降序
	 */
	void refreshHotSearchBoard();

	/**
	 * 收藏：近7日 collect_cnt 降序
	 */
	void refreshCollectBoard();
}
