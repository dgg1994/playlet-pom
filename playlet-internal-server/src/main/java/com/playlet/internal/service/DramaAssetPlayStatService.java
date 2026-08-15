package com.playlet.internal.service;

import com.playlet.internal.constants.RedisKeyConstants;
import com.playlet.internal.constants.TheaterConstants;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.utils.RedisUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 剧集曝光/完播统计：挂在观看上报上，Redis 去重后异步落库。
 */
@Slf4j
@Service
public class DramaAssetPlayStatService {

	@Autowired
	private DramaAssetDao dramaAssetDao;
	@Autowired
	private RedisUtil redisUtil;

	/**
	 * 观看上报后累计：有 episodeId 记曝光；进度达阈值记完播。
	 */
	@Async("asyncExecutor")
	public void onWatchReport(Integer uid, String episodeId, Integer watchProgress, Integer clientDurationSec) {
		if (uid == null || StringUtils.isEmpty(episodeId)) {
			return;
		}
		Integer assetId = parseAssetId(episodeId);
		if (assetId == null) {
			return;
		}
		try {
			// 曝光：同一用户同一集每日最多 +1
			tryIncrExposure(uid, assetId);
			// 完播：进度达阈值且同一用户同一集去重
			tryIncrComplete(uid, assetId, watchProgress, clientDurationSec);
		} catch (Exception e) {
			log.error("drama asset play stat failed uid={} assetId={}", uid, assetId, e);
		}
	}

	private void tryIncrExposure(Integer uid, Integer assetId) {
		String key = RedisKeyConstants.PLAY_EXPOSE_DEDUP + uid + ":" + assetId;
		if (!redisUtil.setIfAbsent(key, "1", TheaterConstants.PLAY_EXPOSE_DEDUP_TTL_SEC)) {
			return;
		}
		dramaAssetDao.incrExposureCount(assetId);
		log.info("drama asset exposure +1 assetId={} uid={}", assetId, uid);
	}

	private void tryIncrComplete(Integer uid, Integer assetId, Integer watchProgress, Integer clientDurationSec) {
		if (watchProgress == null || watchProgress <= 0) {
			return;
		}
		int durationSec = resolveDurationSec(assetId, clientDurationSec);
		if (durationSec <= 0) {
			return;
		}
		// progress * 100 >= duration * percent
		boolean reached = (long) watchProgress * 100L
				>= (long) durationSec * TheaterConstants.COMPLETE_PROGRESS_PERCENT;
		if (!reached) {
			return;
		}
		String key = RedisKeyConstants.PLAY_COMPLETE_DEDUP + uid + ":" + assetId;
		if (!redisUtil.setIfAbsent(key, "1", TheaterConstants.PLAY_COMPLETE_DEDUP_TTL_SEC)) {
			return;
		}
		dramaAssetDao.incrCompleteCount(assetId);
		log.info("drama asset complete +1 assetId={} uid={} progress={} duration={}",
				assetId, uid, watchProgress, durationSec);
	}

	/** 优先库内 duration_seconds，否则用客户端上报时长（再裁剪） */
	private int resolveDurationSec(Integer assetId, Integer clientDurationSec) {
		DramaAssetEntity asset = dramaAssetDao.selectById(assetId);
		if (asset != null && asset.getDurationSeconds() != null && asset.getDurationSeconds() > 0) {
			return asset.getDurationSeconds();
		}
		if (clientDurationSec == null || clientDurationSec <= 0) {
			return TheaterConstants.DEFAULT_EPISODE_DURATION_SEC;
		}
		return Math.min(TheaterConstants.MAX_EPISODE_DURATION_SEC,
				Math.max(TheaterConstants.MIN_EPISODE_DURATION_SEC, clientDurationSec));
	}

	private static Integer parseAssetId(String episodeId) {
		try {
			return Integer.valueOf(episodeId.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
