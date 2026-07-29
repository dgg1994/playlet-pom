package com.playlet.internal.scheduled;

import com.playlet.internal.service.RankAlgoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 算法榜定时刷新：热播 / 新剧 / 飙升 / 推荐 / 热搜 / 收藏
 */
@Slf4j
@Component
public class RankAlgoScheduledTasks {

	@Autowired
	private RankAlgoService rankAlgoService;

	/** 每 5 分钟刷新一次 */
	@Scheduled(cron = "0 0/5 * * * ?")
	public void refreshP0Boards() {
		try {
			log.info("rank algo refresh start");
			rankAlgoService.refreshAllP0();
			log.info("rank algo refresh done");
		} catch (Exception e) {
			log.error("rank algo refresh failed", e);
		}
	}
}
