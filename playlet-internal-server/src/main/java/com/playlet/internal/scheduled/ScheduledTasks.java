package com.playlet.internal.scheduled;

import com.playlet.internal.service.support.IdleCoinClearService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：久未登录清币等。
 */
@Slf4j
@Component
public class ScheduledTasks {

	@Autowired
	private IdleCoinClearService idleCoinClearService;

	/** 每天 3:30 扫描久未登录用户清币（受 coin_global_config 开关控制） */
	@Scheduled(cron = "0 30 3 * * ?")
	public void clearIdleCoin() {
		try {
			int n = idleCoinClearService.clearIdleUsers();
			log.info("scheduled idle coin clear finished count={}", n);
		} catch (Exception e) {
			log.error("scheduled idle coin clear failed", e);
		}
	}
}
