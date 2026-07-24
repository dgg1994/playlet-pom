package com.playlet.internal.scheduled;

import com.playlet.internal.service.RankAlgoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * P0 算法榜定时刷新：热播 / 新剧 / 收藏
 */
@Slf4j
@Component
public class RankAlgoScheduledTasks {

	@Autowired
	private RankAlgoService rankAlgoService;

	/** 每 30 分钟刷新一次 */
	@Scheduled(cron = "0 0/30 * * * ?")
	public void refreshP0Boards() {
		try {
			log.info("rank algo P0 refresh start");
			rankAlgoService.refreshAllP0();
			log.info("rank algo P0 refresh done");
		} catch (Exception e) {
			log.error("rank algo P0 refresh failed", e);
		}
	}
}
