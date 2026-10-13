package com.playlet.internal.service.impl;

import com.playlet.internal.api.response.ShareInfoResp;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.ShareConstants;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.service.ShareApiService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 播放页内容分享：下发分享素材与落地链接。
 */
@Slf4j
@RestController
@CrossOrigin
public class ShareApiServiceImpl implements ShareApiService {

	@Value("${playlet.share.h5-base:}")
	private String h5Base;

	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private DramaAssetDao dramaAssetDao;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	public ResponseBase info(@RequestParam(value = "dramaId", required = false) Integer dramaId,
			@RequestParam(value = "videoId", required = false) Integer videoId) {
		if (dramaId == null && videoId == null) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}

		String base = trimTrailingSlash(h5Base);
		if (StringUtils.isBlank(base)) {
			log.error("share info h5-base not configured");
			return setResultError(I18nUtil.getMessage("share_h5_base_not_configured"));
		}

		Integer resolvedDramaId = dramaId;
		Integer resolvedVideoId = null;
		Integer setNum = null;

		// 仅传集：由上架集反查剧
		if (resolvedDramaId == null) {
			DramaAssetEntity asset = dramaAssetDao.findOnShelfById(videoId);
			if (asset == null || asset.getDramaId() == null) {
				return setResultError(I18nUtil.getMessage("drama_null"));
			}
			resolvedDramaId = asset.getDramaId();
			resolvedVideoId = asset.getId();
			setNum = asset.getSetNum();
		} else if (videoId != null) {
			// 集合法且属于本剧才带上；否则降级整剧
			DramaAssetEntity asset = dramaAssetDao.findOnShelfById(videoId);
			boolean sameDrama = asset != null && resolvedDramaId.equals(asset.getDramaId());
			if (sameDrama) {
				resolvedVideoId = asset.getId();
				setNum = asset.getSetNum();
			} else {
				log.info("share info video degrade dramaId={} videoId={}", resolvedDramaId, videoId);
			}
		}

		DramaEntity drama = dramaDao.findOnlineByDramaId(resolvedDramaId);
		if (drama == null) {
			return setResultError(I18nUtil.getMessage("drama_null"));
		}

		ShareInfoResp resp = new ShareInfoResp();
		resp.setDramaId(drama.getId());
		resp.setVideoId(resolvedVideoId);
		resp.setTitle(resolveTitle(drama.getDramaTitle()));
		resp.setDesc(resolveDesc(drama.getDescriptionInfo(), setNum));
		resp.setCoverUrl(mediaUrlService.sign(drama.getCoverUrl()));
		resp.setShareUrl(buildShareUrl(base, drama.getId(), resolvedVideoId));
		resp.setShareScore(drama.getShareScore() == null ? 0L : drama.getShareScore());

		log.info("share info ok dramaId={} videoId={}", drama.getId(), resolvedVideoId);
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	private static String resolveTitle(String dramaTitle) {
		if (StringUtils.isNotBlank(dramaTitle)) {
			return dramaTitle.trim();
		}
		return I18nUtil.getMessage("share_default_title");
	}

	/** 简介截断；有集号时前置「第N集」 */
	private static String resolveDesc(String descriptionInfo, Integer setNum) {
		StringBuilder sb = new StringBuilder();
		if (setNum != null) {
			sb.append(I18nUtil.getMessage("share_episode_fmt", setNum));
		}
		if (StringUtils.isNotBlank(descriptionInfo)) {
			String plain = descriptionInfo.trim();
			if (plain.length() > ShareConstants.DESC_MAX_LEN) {
				plain = plain.substring(0, ShareConstants.DESC_MAX_LEN);
			}
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(plain);
		}
		if (sb.length() == 0) {
			return I18nUtil.getMessage("share_default_desc");
		}
		return sb.toString();
	}

	private static String buildShareUrl(String h5Base, Integer dramaId, Integer videoId) {
		StringBuilder url = new StringBuilder(h5Base)
				.append(ShareConstants.H5_PLAY_PATH)
				.append('?')
				.append(ShareConstants.PARAM_DRAMA_ID).append('=').append(dramaId)
				.append('&')
				.append(ShareConstants.PARAM_CHANNEL).append('=').append(ShareConstants.CHANNEL_APP);
		if (videoId != null) {
			url.append('&').append(ShareConstants.PARAM_VIDEO_ID).append('=').append(videoId);
		}
		return url.toString();
	}

	private static String trimTrailingSlash(String base) {
		if (base == null) {
			return "";
		}
		String trimmed = base.trim();
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}
}
