package com.playlet.internal.service;

import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.utils.QiniuUploadUtils;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 剧集时长：release 后异步拉七牛 avinfo 落库。
 */
@Slf4j
@Service
public class DramaAssetDurationService {

	@Autowired
	private DramaAssetDao dramaAssetDao;

	/**
	 * 对原始上传 key 调 avinfo，写入 drama_asset.duration_seconds。
	 * 失败只打日志，不影响发布主流程。
	 */
	@Async("asyncExecutor")
	public void fillDurationFromAvinfo(Integer assetId, String sourceKey) {
		if (assetId == null || StringUtils.isEmpty(sourceKey)) {
			return;
		}
		try {
			Integer seconds = QiniuUploadUtils.fetchDurationSeconds(sourceKey);
			if (seconds == null || seconds <= 0) {
				log.warn("drama asset duration empty assetId={} key={}", assetId, sourceKey);
				return;
			}
			dramaAssetDao.updateDurationSeconds(assetId, seconds);
			log.info("drama asset duration filled assetId={} durationSeconds={}", assetId, seconds);
		} catch (Exception e) {
			log.error("drama asset duration fill failed assetId={} key={}", assetId, sourceKey, e);
		}
	}
}
