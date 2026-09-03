package com.playlet.oversea.scheduled;

import com.playlet.oversea.service.third.WalletUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：KYC 状态轮询等。
 */
@Slf4j
@Component
public class ScheduledTasks {

	@Autowired
	private WalletUserService walletUserService;

	/**
	 * 轮询 KYC 认证中的开卡申请（对齐 onetoken findKycState，每 2 分钟）。
	 */
	@Scheduled(cron = "0 0/2 * * * *")
	public void pollKycStateForCardApply() {
		try {
			walletUserService.pollPendingKycApplies();
		} catch (Exception e) {
			log.error("scheduled poll kyc state failed", e);
		}
	}
}
