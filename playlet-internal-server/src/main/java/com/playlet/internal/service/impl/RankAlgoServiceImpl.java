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

	/**
	 * 依次刷新全部算法榜：热播 → 新剧 → 飙升 → 推荐 → 热搜 → 收藏。
	 * 各榜独立读写 rank_list；某一榜缺失/停用时跳过，不影响其它榜。
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
	 * 热播榜（rb_hot_play）
	 * <p>
	 * 数据：drama_rank_stat_daily 近 {@link RankBoardGroupConstants#WINDOW_DAYS_HOT} 天聚合。
	 * <p>
	 * 公式：
	 * {@code score = valid_seconds×0.6 + collect_cnt×0.2 + like_cnt×0.1 + play_pv×0.1}
	 * <p>
	 * 说明：偏「谁在看」；有效观看权重最高，辅以收藏/点赞/播放次数。
	 * 候选：已上架且未删除；按 score 降序，取 topN 覆盖写入 rank_list。
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void refreshHotPlayBoard() {
		refreshBoard(RankBoardGroupConstants.HOT_PLAY, RankBoardGroupConstants.WINDOW_DAYS_HOT, null);
	}

	/**
	 * 新剧榜（rb_new）
	 * <p>
	 * 数据：与热播相同的近 {@link RankBoardGroupConstants#WINDOW_DAYS_HOT} 天行为聚合；
	 * 额外限定 drama.setTime ≥ 当前 − {@link RankBoardGroupConstants#WINDOW_DAYS_NEW} 天。
	 * <p>
	 * 公式：与热播相同
	 * {@code score = valid_seconds×0.6 + collect_cnt×0.2 + like_cnt×0.1 + play_pv×0.1}
	 * <p>
	 * 说明：在「新上架窗口」内比窗口期表现，老剧不参与。
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void refreshNewBoard() {
		ZoneId zone = ZoneId.of(RankBoardGroupConstants.TIMEZONE);
		String newSince = LocalDate.now(zone)
				.minusDays(RankBoardGroupConstants.WINDOW_DAYS_NEW)
				.atStartOfDay()
				.format(DATE_TIME_FMT);
		refreshBoard(RankBoardGroupConstants.NEW, RankBoardGroupConstants.WINDOW_DAYS_HOT, newSince);
	}

	/**
	 * 飙升榜（rb_rising）
	 * <p>
	 * 窗口：近 {@link RankBoardGroupConstants#WINDOW_DAYS_RISING} 天 vs 前同等天数（共 2N 天对比）。
	 * <p>
	 * 公式：
	 * {@code score = (recentValid − prevValid)×0.7 + (recentPlay − prevPlay)×0.3}
	 * <p>
	 * 过滤：近窗 valid_seconds ≥ {@link RankBoardGroupConstants#RISING_MIN_VALID_SECONDS}，
	 * 且 recentValid &gt; prevValid（只保留真正上涨的剧）。
	 * <p>
	 * 说明：刻画短周期增速，与热播「绝对热度」互补。
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
	 * 推荐榜（rb_recommend）
	 * <p>
	 * 数据：近 {@link RankBoardGroupConstants#WINDOW_DAYS_HOT} 天行为聚合
	 * + 上架 {@link RankBoardGroupConstants#RECOMMEND_FRESH_DAYS} 天内的新鲜度加分。
	 * <p>
	 * 公式：
	 * {@code score = valid×0.35 + collect×0.25 + like×0.15 + search×0.10 + play×0.15
	 *          + max(0, freshDays − ageDays) × freshWeightPerDay}
	 * <p>
	 * 候选：有任意窗口行为，或处于新鲜度窗口内；无行为的老剧不入榜。
	 * <p>
	 * 说明：相对热播更均衡（降低观看、提高收藏/搜索），并用新鲜度避免老热剧长期霸榜。
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
	 * 热搜榜（rb_hot_search）
	 * <p>
	 * 数据：近 {@link RankBoardGroupConstants#WINDOW_DAYS_HOT} 天 sum(search_cnt)。
	 * search_cnt 来自剧场标题搜索首页命中（见 DramaRankStatService#onSearch）。
	 * <p>
	 * 公式：{@code score = search_cnt}（降序；需 search_cnt &gt; 0）。
	 * <p>
	 * 说明：反映搜索热度，与观看类榜解耦。
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
	 * 收藏榜（rb_collect）
	 * <p>
	 * 数据：近 {@link RankBoardGroupConstants#WINDOW_DAYS_HOT} 天 sum(collect_cnt)（净收藏，取消为负后下限 0）。
	 * <p>
	 * 公式：{@code score = collect_cnt}（降序；需 collect_cnt &gt; 0）。
	 * <p>
	 * 说明：单指标榜，突出收藏意愿。
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
	 * 热播/新剧共用刷新：按窗口聚合综合分写 rank_list。
	 *
	 * @param groupId    榜 group_id
	 * @param windowDays 行为统计窗口天数
	 * @param newSince   非空时限定 drama.setTime ≥ newSince（新剧榜）
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

	/** 先删后插，按候选顺序写入 rank_no=1..N */
	private void rewriteRankList(String groupId, List<DramaRankAggRow> rows) {
		rankListDao.deleteByBoardGroupId(groupId);
		if (rows == null || rows.isEmpty()) {
			return;
		}
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

	/** 统计窗口起始日（含当天）：today − (windowDays−1)，时区 Asia/Shanghai */
	private String fromDate(int windowDays) {
		return LocalDate.now(ZoneId.of(RankBoardGroupConstants.TIMEZONE))
				.minusDays(Math.max(windowDays - 1, 0))
				.format(DATE_FMT);
	}
}
