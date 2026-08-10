package com.playlet.internal.service.impl;

import com.playlet.internal.api.response.DramaRankAggRow;
import com.playlet.internal.constants.RankBoardGroupConstants;
import com.playlet.internal.dao.drama.DramaRankStatDailyDao;
import com.playlet.internal.dao.drama.RankBoardDao;
import com.playlet.internal.dao.drama.RankListDao;
import com.playlet.internal.entity.drama.RankBoardEntity;
import com.playlet.internal.entity.drama.RankListEntity;
import com.playlet.internal.service.RankAlgoService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.TheaterHomeCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static com.playlet.internal.constants.RankBoardGroupConstants.DATE_FMT;
import static com.playlet.internal.constants.RankBoardGroupConstants.DATE_TIME_FMT;

@Slf4j
@Service
public class RankAlgoServiceImpl implements RankAlgoService {

	@Autowired
	private RankBoardDao rankBoardDao;
	@Autowired
	private RankListDao rankListDao;
	@Autowired
	private DramaRankStatDailyDao dramaRankStatDailyDao;
	@Autowired
	private TheaterHomeCacheHelper theaterHomeCacheHelper;

	/**
	 * 刷一遍全部算法榜（热播、新剧、飙升、推荐、热搜、收藏）。
	 * 某一榜没开或找不到就跳过，不影响其他榜。
	 */
	@Override
	public void refreshAllP0() {
		refreshHotPlayBoard();
		refreshNewBoard();
		refreshRisingBoard();
		refreshRecommendBoard();
		refreshHotSearchBoard();
		refreshCollectBoard();
	}

	/**
	 * 热播榜：看谁最近最火。
	 * <p>
	 * 看近 N 天数据，分数：观看时长 60% + 收藏 20% + 点赞 10% + 播放次数 10%。
	 * 分数高的排前面，取 topN 写入榜单。
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void refreshHotPlayBoard() {
		refreshBoard(RankBoardGroupConstants.HOT_PLAY, RankBoardGroupConstants.WINDOW_DAYS_HOT, null);
	}

	/**
	 * 新剧榜：只在「新上架」的剧里按上架时间排。
	 * <p>
	 * 近 14 天上架的剧，setTime 越新越靠前；不再用热播综合分。
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void refreshNewBoard() {
		RankBoardEntity board = rankBoardDao.findOneByGroupId(RankBoardGroupConstants.NEW);
		if (board == null || board.getStatus() == null || board.getStatus() != 1) {
			log.info("skip board {}: missing or disabled", RankBoardGroupConstants.NEW);
			return;
		}
		int topN = board.getTopN() == null || board.getTopN() < 1 ? 100 : board.getTopN();
		ZoneId zone = ZoneId.of(RankBoardGroupConstants.TIMEZONE);
		String newSince = LocalDate.now(zone)
				.minusDays(RankBoardGroupConstants.WINDOW_DAYS_NEW)
				.atStartOfDay()
				.format(DATE_TIME_FMT);
		List<DramaRankAggRow> rows = dramaRankStatDailyDao.findNewBoardCandidates(newSince, topN);
		rewriteRankList(RankBoardGroupConstants.NEW, rows);
		log.info("refresh board {} size={}", RankBoardGroupConstants.NEW, rows == null ? 0 : rows.size());
	}

	/**
	 * 飙升榜：看谁涨得快（比的是增长速度，不是绝对热度）。
	 * <p>
	 * 对比「最近 N 天」和「再往前 N 天」：观看时长增量占 70%，播放次数增量占 30%。
	 * 最近必须比前一段更热，且观看量要够门槛，才进榜。
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void refreshRisingBoard() {
		RankBoardEntity board = rankBoardDao.findOneByGroupId(RankBoardGroupConstants.RISING);
		if (board == null || board.getStatus() == null || board.getStatus() != 1) {
			log.info("skip rising board: missing or disabled");
			return;
		}
		int topN = board.getTopN() == null || board.getTopN() < 1 ? 100 : board.getTopN();
		ZoneId zone = ZoneId.of(RankBoardGroupConstants.TIMEZONE);
		LocalDate today = LocalDate.now(zone);
		int n = RankBoardGroupConstants.WINDOW_DAYS_RISING;
		String todayStr = today.format(DATE_FMT);
		String recentFrom = today.minusDays(n - 1L).format(DATE_FMT);
		String prevTo = today.minusDays(n).format(DATE_FMT);
		String prevFrom = today.minusDays(2L * n - 1).format(DATE_FMT);
		List<DramaRankAggRow> rows = dramaRankStatDailyDao.findRisingCandidates(
				todayStr, recentFrom, prevFrom, prevTo,
				RankBoardGroupConstants.RISING_MIN_VALID_SECONDS, topN);
		rewriteRankList(RankBoardGroupConstants.RISING, rows);
		log.info("refresh rising board size={}", rows == null ? 0 : rows.size());
	}

	/**
	 * 推荐榜：综合热度 + 一点「新剧加分」。
	 * <p>
	 * 观看、收藏、点赞、搜索、播放都算分，权重更均衡（不像热播几乎只认观看）。
	 * 上架不久的剧额外加分，避免老热剧一直占榜。
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void refreshRecommendBoard() {
		RankBoardEntity board = rankBoardDao.findOneByGroupId(RankBoardGroupConstants.RECOMMEND);
		if (board == null || board.getStatus() == null || board.getStatus() != 1) {
			log.info("skip recommend board: missing or disabled");
			return;
		}
		int topN = board.getTopN() == null || board.getTopN() < 1 ? 100 : board.getTopN();
		ZoneId zone = ZoneId.of(RankBoardGroupConstants.TIMEZONE);
		String fromDate = fromDate(RankBoardGroupConstants.WINDOW_DAYS_HOT);
		String freshSince = LocalDate.now(zone)
				.minusDays(RankBoardGroupConstants.RECOMMEND_FRESH_DAYS)
				.atStartOfDay()
				.format(DATE_TIME_FMT);
		List<DramaRankAggRow> rows = dramaRankStatDailyDao.findRecommendCandidates(
				fromDate,
				freshSince,
				RankBoardGroupConstants.RECOMMEND_FRESH_DAYS,
				RankBoardGroupConstants.RECOMMEND_FRESH_WEIGHT_PER_DAY,
				topN);
		rewriteRankList(RankBoardGroupConstants.RECOMMEND, rows);
		log.info("refresh recommend board size={}", rows == null ? 0 : rows.size());
	}

	/**
	 * 热搜榜：谁被搜得多就排谁前面。
	 * <p>
	 * 只看近 N 天搜索次数，和观看热度无关。
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void refreshHotSearchBoard() {
		RankBoardEntity board = rankBoardDao.findOneByGroupId(RankBoardGroupConstants.HOT_SEARCH);
		if (board == null || board.getStatus() == null || board.getStatus() != 1) {
			log.info("skip hot search board: missing or disabled");
			return;
		}
		int topN = board.getTopN() == null || board.getTopN() < 1 ? 100 : board.getTopN();
		String fromDate = fromDate(RankBoardGroupConstants.WINDOW_DAYS_HOT);
		List<DramaRankAggRow> rows = dramaRankStatDailyDao.findHotSearchCandidates(fromDate, topN);
		rewriteRankList(RankBoardGroupConstants.HOT_SEARCH, rows);
		log.info("refresh hot search board size={}", rows == null ? 0 : rows.size());
	}

	/**
	 * 收藏榜：谁被收藏得多就排谁前面。
	 * <p>
	 * 只看近 N 天净收藏数（取消收藏会扣，最低为 0）。
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void refreshCollectBoard() {
		RankBoardEntity board = rankBoardDao.findOneByGroupId(RankBoardGroupConstants.COLLECT);
		if (board == null || board.getStatus() == null || board.getStatus() != 1) {
			log.info("skip collect board: missing or disabled");
			return;
		}
		int topN = board.getTopN() == null || board.getTopN() < 1 ? 100 : board.getTopN();
		String fromDate = fromDate(RankBoardGroupConstants.WINDOW_DAYS_HOT);
		List<DramaRankAggRow> rows = dramaRankStatDailyDao.findCollectCandidates(fromDate, topN);
		rewriteRankList(RankBoardGroupConstants.COLLECT, rows);
		log.info("refresh collect board size={}", rows == null ? 0 : rows.size());
	}

	/**
	 * 热播：按窗口汇总分数后覆盖写榜。
	 */
	private void refreshBoard(String groupId, int windowDays, String newSince) {
		RankBoardEntity board = rankBoardDao.findOneByGroupId(groupId);
		if (board == null || board.getStatus() == null || board.getStatus() != 1) {
			log.info("skip board {}: missing or disabled", groupId);
			return;
		}
		int topN = board.getTopN() == null || board.getTopN() < 1 ? 100 : board.getTopN();
		String fromDate = fromDate(windowDays);
		List<DramaRankAggRow> rows = dramaRankStatDailyDao.findHotPlayCandidates(fromDate, newSince, topN);
		rewriteRankList(groupId, rows);
		log.info("refresh board {} size={}", groupId, rows == null ? 0 : rows.size());
	}

	/** 清空旧名单，再按分数顺序写入 1、2、3… */
	private void rewriteRankList(String groupId, List<DramaRankAggRow> rows) {
		rankListDao.deleteByBoardGroupId(groupId);
		if (rows != null && !rows.isEmpty()) {
			int rankNo = 1;
			for (DramaRankAggRow row : rows) {
				if (row.getDramaId() == null) {
					continue;
				}
				RankListEntity entity = new RankListEntity();
				entity.setBoardGroupId(groupId);
				entity.setRankNo(rankNo++);
				entity.setDramaId(String.valueOf(row.getDramaId()));
				entity.setStatus(1);
				try {
					GenericityUtil.setDate(entity);
				} catch (Exception e) {
					Date now = new Date();
					entity.setSetTime(now);
					entity.setGmtModified(now);
				}
				rankListDao.insert(entity);
			}
		}
		theaterHomeCacheHelper.invalidateAll();
	}

	/** 统计从哪天开始：今天往前推 windowDays 天（含今天），时区上海 */
	private String fromDate(int windowDays) {
		return LocalDate.now(ZoneId.of(RankBoardGroupConstants.TIMEZONE))
				.minusDays(Math.max(windowDays - 1, 0))
				.format(DATE_FMT);
	}
}
