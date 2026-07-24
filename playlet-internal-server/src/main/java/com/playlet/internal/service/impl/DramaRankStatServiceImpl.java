package com.playlet.internal.service.impl;

import com.playlet.internal.constants.RankBoardGroupConstants;
import com.playlet.internal.dao.drama.DramaRankStatDailyDao;
import com.playlet.internal.service.DramaRankStatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class DramaRankStatServiceImpl implements DramaRankStatService {

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Autowired
	private DramaRankStatDailyDao dramaRankStatDailyDao;

	@Override
	@Async("asyncExecutor")
	public void onWatch(Integer dramaId, int deltaSeconds) {
		if (dramaId == null) {
			return;
		}
		int seconds = Math.max(0, deltaSeconds);
		try {
			dramaRankStatDailyDao.upsertDelta(today(), dramaId, 1, seconds, 0, 0);
		} catch (Exception e) {
			log.warn("rank stat onWatch failed dramaId={}: {}", dramaId, e.getMessage());
		}
	}

	@Override
	@Async("asyncExecutor")
	public void onCollect(Integer dramaId, int delta) {
		if (dramaId == null || delta == 0) {
			return;
		}
		try {
			dramaRankStatDailyDao.upsertDelta(today(), dramaId, 0, 0, delta, 0);
		} catch (Exception e) {
			log.warn("rank stat onCollect failed dramaId={}: {}", dramaId, e.getMessage());
		}
	}

	@Override
	@Async("asyncExecutor")
	public void onLike(Integer dramaId, int delta) {
		if (dramaId == null || delta == 0) {
			return;
		}
		try {
			dramaRankStatDailyDao.upsertDelta(today(), dramaId, 0, 0, 0, delta);
		} catch (Exception e) {
			log.warn("rank stat onLike failed dramaId={}: {}", dramaId, e.getMessage());
		}
	}

	private String today() {
		return LocalDate.now(ZoneId.of(RankBoardGroupConstants.TIMEZONE)).format(DATE_FMT);
	}
}
