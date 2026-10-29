package com.playlet.internal.scheduled;

import com.playlet.internal.constants.RedisKeyConstants;
import com.playlet.internal.service.support.member.MemberAutoRenewSupport;
import com.playlet.internal.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：连续包月到期自动扣款续费。
 * <p>
 * 多实例部署靠 Redis 锁保证同一时刻只有一个实例执行。
 */
@Slf4j
@Component
public class MemberAutoRenewScheduled {

	/** 锁过期时间需大于单次批处理耗时 */
	private static final long LOCK_SEC = 15 * 60L;

	@Autowired
	private MemberAutoRenewSupport memberAutoRenewSupport;
	@Autowired
	private RedisUtil redisUtil;

	/** 每 10 分钟扫描一次到期协议并代扣 */
	@Scheduled(cron = "0 0/10 * * * ?")
	public void processDueRenew() {
		String lockKey = RedisKeyConstants.AUTO_RENEW_LOCK;
		boolean locked = redisUtil.setIfAbsent(lockKey,
				String.valueOf(System.currentTimeMillis()), LOCK_SEC);
		if (!locked) {
			log.info("auto renew skipped, another instance is running");
			return;
		}
		try {
			int[] stat = memberAutoRenewSupport.processDue();
			log.info("auto renew scheduled finished success={} fail={}", stat[0], stat[1]);
		} catch (Exception e) {
			log.error("auto renew scheduled failed", e);
		} finally {
			redisUtil.del(lockKey);
		}
	}
}
