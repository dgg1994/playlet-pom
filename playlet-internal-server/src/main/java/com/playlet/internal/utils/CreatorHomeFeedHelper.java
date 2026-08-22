package com.playlet.internal.utils;

import com.playlet.internal.api.response.CreatorHomeFeedRespEntity;
import com.playlet.internal.api.response.CreatorHomeHotDramaRespEntity;
import com.playlet.internal.api.response.CreatorHomeHotTagAggRow;
import com.playlet.internal.api.response.CreatorHomeHotTagRespEntity;
import com.playlet.internal.api.response.CreatorHomeRankAggRow;
import com.playlet.internal.api.response.CreatorHomeRankItemRespEntity;
import com.playlet.internal.api.response.DramaRankAggRow;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.CreatorConstants;
import com.playlet.internal.dao.creator.CreatorHomeDao;
import com.playlet.internal.dao.drama.DramaRankStatDailyDao;
import com.playlet.internal.enums.CreatorHomeRankTypeEnums;
import com.playlet.internal.enums.LanguageEnums;
import com.playlet.internal.service.MediaUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 作家首页 Feed / 榜单构建（创作者端与管理端共用）。
 */
@Component
public class CreatorHomeFeedHelper {

	@Autowired
	private CreatorHomeDao creatorHomeDao;
	@Autowired
	private DramaRankStatDailyDao dramaRankStatDailyDao;
	@Autowired
	private MediaUrlService mediaUrlService;

	/** 近期热点剧 + 热点题材 */
	public CreatorHomeFeedRespEntity buildFeed() {
		CreatorHomeFeedRespEntity resp = new CreatorHomeFeedRespEntity();
		resp.setHotDramas(buildHotDramas());
		resp.setHotTags(buildHotTags());
		return resp;
	}

	/** 影响力 / 成长力榜列表 */
	public List<CreatorHomeRankItemRespEntity> buildRank(CreatorHomeRankTypeEnums rankType) {
		List<CreatorHomeRankAggRow> rows;
		if (CreatorHomeRankTypeEnums.GROWTH.equals(rankType)) {
			rows = queryGrowthRows();
		} else {
			String fromDate = windowFromDate(CreatorConstants.INFLUENCE_WINDOW_DAYS);
			rows = creatorHomeDao.findInfluenceRank(fromDate, CreatorConstants.HOME_RANK_LIMIT);
		}
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}
		List<CreatorHomeRankItemRespEntity> list = new ArrayList<>(rows.size());
		int rankNo = 1;
		for (CreatorHomeRankAggRow row : rows) {
			CreatorHomeRankItemRespEntity item = new CreatorHomeRankItemRespEntity();
			item.setRankNo(rankNo++);
			item.setCreatorId(row.getCreatorId());
			item.setNickname(row.getNickname());
			item.setScore(row.getScore() == null ? 0L : row.getScore());
			list.add(item);
		}
		return list;
	}

	/** 全站近期热点剧（与 C 端热播同源） */
	private List<CreatorHomeHotDramaRespEntity> buildHotDramas() {
		String fromDate = windowFromDate(CreatorConstants.INFLUENCE_WINDOW_DAYS);
		List<DramaRankAggRow> rows = dramaRankStatDailyDao.findHotPlayCandidates(fromDate, null,
				CreatorConstants.HOME_HOT_DRAMA_LIMIT);
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}
		List<CreatorHomeHotDramaRespEntity> list = new ArrayList<>(rows.size());
		for (DramaRankAggRow row : rows) {
			CreatorHomeHotDramaRespEntity item = new CreatorHomeHotDramaRespEntity();
			item.setDramaId(row.getDramaId());
			item.setDramaTitle(row.getDramaTitle());
			item.setCoverUrl(mediaUrlService.sign(row.getCoverUrl()));
			list.add(item);
		}
		return list;
	}

	/** 近窗有播放剧的标签气泡；命中数达阈值打火 */
	private List<CreatorHomeHotTagRespEntity> buildHotTags() {
		String langue = resolveLangue();
		String fromDate = windowFromDate(CreatorConstants.INFLUENCE_WINDOW_DAYS);
		List<CreatorHomeHotTagAggRow> rows = creatorHomeDao.findHotTags(fromDate, langue,
				CreatorConstants.HOME_HOT_TAG_LIMIT);
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}
		List<CreatorHomeHotTagRespEntity> list = new ArrayList<>(rows.size());
		for (CreatorHomeHotTagAggRow row : rows) {
			CreatorHomeHotTagRespEntity item = new CreatorHomeHotTagRespEntity();
			item.setGroupId(row.getGroupId());
			item.setTagName(row.getTagName());
			long hit = row.getHitCnt() == null ? 0L : row.getHitCnt();
			item.setHotFlag(hit >= CreatorConstants.HOT_TAG_FIRE_MIN_CNT ? 1 : 0);
			list.add(item);
		}
		return list;
	}

	private List<CreatorHomeRankAggRow> queryGrowthRows() {
		LocalDate today = CreatorBizUtils.today();
		int window = CreatorConstants.GROWTH_WINDOW_DAYS;
		String todayStr = CreatorBizUtils.formatDate(today);
		String recentFrom = CreatorBizUtils.formatDate(today.minusDays(window - 1L));
		String prevTo = CreatorBizUtils.formatDate(today.minusDays(window));
		String prevFrom = CreatorBizUtils.formatDate(today.minusDays(window * 2L - 1L));
		return creatorHomeDao.findGrowthRank(todayStr, recentFrom, prevFrom, prevTo,
				CreatorConstants.GROWTH_MIN_RECENT_SECONDS, CreatorConstants.HOME_RANK_LIMIT);
	}

	private static String windowFromDate(int windowDays) {
		return CreatorBizUtils.formatDate(CreatorBizUtils.today().minusDays(Math.max(windowDays - 1, 0)));
	}

	private static String resolveLangue() {
		String langue = LanguageContext.getLanguage();
		if (StringUtils.isEmpty(langue)) {
			return LanguageEnums.DEFAULT_LANGUE;
		}
		return langue;
	}
}
