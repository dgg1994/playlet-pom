package com.playlet.oversea.service.support;

import com.playlet.oversea.constants.RankBoardGroupConstants;
import com.playlet.oversea.dao.ops.UserActiveDailyDao;
import com.playlet.oversea.dao.ops.UserPlayDailyDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * C 端日活 / 日播放打点（Asia/Shanghai 自然日）。
 */
@Slf4j
@Service
public class UserActiveStatService {

	@Autowired
	private UserActiveDailyDao userActiveDailyDao;
	@Autowired
	private UserPlayDailyDao userPlayDailyDao;

	/** 标记当日活跃（登录/心跳/观看）。 */
	public void markActive(Integer uid) {
		if (uid == null || uid <= 0) {
			return;
		}
		try {
			userActiveDailyDao.upsertActive(today(), uid);
		} catch (Exception e) {
			log.warn("markActive failed uid={}: {}", uid, e.getMessage());
		}
	}

	/** 累计当日有效播放秒。 */
	public void addPlaySeconds(Integer uid, int deltaSeconds) {
		if (uid == null || uid <= 0 || deltaSeconds <= 0) {
			return;
		}
		try {
			String bizDate = today();
			userPlayDailyDao.addPlaySeconds(bizDate, uid, deltaSeconds);
			userActiveDailyDao.upsertActive(bizDate, uid);
		} catch (Exception e) {
			log.warn("addPlaySeconds failed uid={} delta={}: {}", uid, deltaSeconds, e.getMessage());
		}
	}

	public static String today() {
		return LocalDate.now(ZoneId.of(RankBoardGroupConstants.TIMEZONE))
				.format(RankBoardGroupConstants.DATE_FMT);
	}
}
