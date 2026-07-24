package com.playlet.internal.service.impl;

import com.playlet.internal.api.response.DramaRankAggRow;
import com.playlet.internal.constants.RankBoardGroupConstants;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaRankStatDailyDao;
import com.playlet.internal.dao.drama.RankBoardDao;
import com.playlet.internal.dao.drama.RankListDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.RankBoardEntity;
import com.playlet.internal.entity.drama.RankListEntity;
import com.playlet.internal.service.RankAlgoService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class RankAlgoServiceImpl implements RankAlgoService {

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private RankBoardDao rankBoardDao;
	@Autowired
	private RankListDao rankListDao;
	@Autowired
	private DramaRankStatDailyDao dramaRankStatDailyDao;
	@Autowired
	private DramaAssetDao dramaAssetDao;

	@Override
	public void refreshAllP0() {
		refreshHotPlayBoard();
		refreshNewBoard();
		refreshCollectBoard();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void refreshHotPlayBoard() {
		refreshBoard(RankBoardGroupConstants.HOT_PLAY, RankBoardGroupConstants.WINDOW_DAYS_HOT, null);
	}

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
			entity.setScore(row.getAlgoScore() == null ? BigDecimal.ZERO : row.getAlgoScore());
			entity.setTitle(row.getDramaTitle());
			entity.setCoverUrl(resolveCover(row));
			entity.setHotScoreText(row.getHotScoreText());
			entity.setTotalEpisodes(row.getTotalEpisodes());
			entity.setFinished(row.getFinishedState());
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

	private String resolveCover(DramaRankAggRow row) {
		try {
			DramaAssetEntity asset = dramaAssetDao.findEnabledByDramaId(row.getDramaId());
			if (asset != null && StringUtils.isNotEmpty(asset.getVideoUrl())) {
				return asset.getVideoUrl();
			}
		} catch (Exception ignored) {
			// ignore
		}
		return row.getCoverUrl();
	}

	private String fromDate(int windowDays) {
		return LocalDate.now(ZoneId.of(RankBoardGroupConstants.TIMEZONE))
				.minusDays(Math.max(windowDays - 1, 0))
				.format(DATE_FMT);
	}
}
