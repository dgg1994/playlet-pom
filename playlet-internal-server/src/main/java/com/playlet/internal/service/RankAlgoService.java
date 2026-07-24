package com.playlet.internal.service;

/**
 * 算法榜刷新：热播 / 新剧 / 收藏
 */
public interface RankAlgoService {

	/** 刷新全部 P0 算法榜 */
	void refreshAllP0();

	/**
	 * 刷新热播
	 */
	void refreshHotPlayBoard();

	/**
	 * 刷新新剧
	 */
	void refreshNewBoard();

	/**
	 * 刷新收藏
	 */
	void refreshCollectBoard();
}
