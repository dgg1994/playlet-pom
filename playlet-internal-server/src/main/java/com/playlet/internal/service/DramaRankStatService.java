package com.playlet.internal.service;

/**
 * 榜单日聚合打点（观看/收藏/点赞/搜索）
 */
public interface DramaRankStatService {

	/** 观看上报：play_pv+1，valid_seconds+=delta（delta&lt;=0 时只记 pv） */
	void onWatch(Integer dramaId, int deltaSeconds);

	/** 收藏 +1 / 取消 -1 */
	void onCollect(Integer dramaId, int delta);

	/** 点赞 +1 / 取消 -1 */
	void onLike(Integer dramaId, int delta);

	/** 搜索命中 +delta（热搜榜） */
	void onSearch(Integer dramaId, int delta);
}
