package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.CreatorDramaAssetRespEntity;
import com.playlet.internal.api.response.CreatorDramaInfoRespEntity;
import com.playlet.internal.api.response.CreatorDramaListRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.creator.CreatorDramaDao;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.TagDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.DramaAssetAuditStatusEnums;
import com.playlet.internal.query.creator.CreatorDramaListQuery;
import com.playlet.internal.service.CreatorDramaService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.utils.CreatorTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 作家端作品只读：列表/详情均按 belong_user = 当前作家过滤。
 */
@Slf4j
@RestController
@CrossOrigin
public class CreatorDramaServiceImpl extends BaseApiService implements CreatorDramaService {

	@Autowired
	private CreatorDramaDao creatorDramaDao;
	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private DramaAssetDao dramaAssetDao;
	@Autowired
	private TagDao tagDao;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	public ResponseBase findList(@RequestBody(required = false) CreatorDramaListQuery query,
			HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (query == null) {
			query = new CreatorDramaListQuery();
		}
		if (StringUtils.isNotEmpty(query.getDramaTitle())) {
			query.setDramaTitle(query.getDramaTitle().trim());
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<CreatorDramaListRespEntity> list = creatorDramaDao.findList(query, account.getId());
		if (list == null) {
			list = Collections.emptyList();
		}
		for (CreatorDramaListRespEntity item : list) {
			item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
		}
		log.info("creator drama findList creatorId={} size={}", account.getId(), list.size());
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase findInfo(@RequestParam("id") Integer id, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		DramaEntity drama = dramaDao.selectById(id);
		if (drama == null || DeleteStateEnum.DELETE.getIndex().equals(drama.getDeleteState())) {
			return setResultError(I18nUtil.getMessage("drama_null"));
		}
		// 无归属或非本人：不泄露是否存在
		if (drama.getBelongUser() == null || !drama.getBelongUser().equals(account.getId())) {
			return setResultError(I18nUtil.getMessage("purview_error_null"));
		}
		CreatorDramaInfoRespEntity resp = toInfoResp(drama, LanguageContext.getLanguage());
		log.info("creator drama findInfo creatorId={} dramaId={}", account.getId(), id);
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	private CreatorDramaInfoRespEntity toInfoResp(DramaEntity drama, String language) {
		CreatorDramaInfoRespEntity resp = new CreatorDramaInfoRespEntity();
		resp.setId(drama.getId());
		resp.setDramaTitle(drama.getDramaTitle());
		resp.setCoverUrl(mediaUrlService.sign(drama.getCoverUrl()));
		resp.setProducerFirm(drama.getProducerFirm());
		resp.setDescriptionInfo(drama.getDescriptionInfo());
		resp.setTotalEpisodes(drama.getTotalEpisodes());
		resp.setFinishedState(drama.getFinishedState());
		resp.setVideoType(drama.getVideoType());
		resp.setAuditStatus(drama.getAuditStatus());
		resp.setShelfStatus(drama.getShelfStatus());
		resp.setAuditRejectReason(drama.getAuditRejectReason());
		resp.setAppealStatus(drama.getAppealStatus());
		resp.setAppealReason(drama.getAppealReason());
		resp.setAppealTime(drama.getAppealTime());
		resp.setSetTime(drama.getSetTime());
		resp.setGmtModified(drama.getGmtModified());
		resp.setTagList(tagDao.findGroupLang(language, drama.getId()));
		List<DramaAssetEntity> assets = dramaAssetDao.findNotDeletedByDramaId(drama.getId());
		List<CreatorDramaAssetRespEntity> assetList = new ArrayList<>();
		if (assets != null) {
			for (DramaAssetEntity asset : assets) {
				assetList.add(toAssetResp(asset));
			}
		}
		resp.setAssetList(assetList);
		resp.setUploadSetNum(assetList.size());
		return resp;
	}

	private CreatorDramaAssetRespEntity toAssetResp(DramaAssetEntity asset) {
		CreatorDramaAssetRespEntity resp = new CreatorDramaAssetRespEntity();
		resp.setId(asset.getId());
		resp.setDramaId(asset.getDramaId());
		resp.setSetNum(asset.getSetNum());
		resp.setVideoName(asset.getVideoName());
		resp.setVideoUrl(mediaUrlService.signVideo(asset.getVideoUrl()));
		resp.setAuditStatus(asset.getAuditStatus());
		resp.setAuditStatusName(DramaAssetAuditStatusEnums.getLabel(asset.getAuditStatus()));
		resp.setShelfStatus(asset.getShelfStatus());
		resp.setAuditRejectReason(asset.getAuditRejectReason());
		resp.setSetTime(asset.getSetTime());
		resp.setDurationSeconds(asset.getDurationSeconds());
		resp.setDurationText(formatDuration(asset.getDurationSeconds()));
		long exposure = asset.getExposureCount() == null ? 0L : asset.getExposureCount();
		long complete = asset.getCompleteCount() == null ? 0L : asset.getCompleteCount();
		resp.setExposureCount(exposure);
		resp.setCompleteCount(complete);
		// 完播率 = 完播 / 曝光 * 100，保留 1 位小数
		if (exposure <= 0) {
			resp.setCompleteRate(0.0);
		} else {
			resp.setCompleteRate(Math.round(complete * 1000.0 / exposure) / 10.0);
		}
		return resp;
	}

	/** 秒数转 7'55"；空则不展示 */
	private static String formatDuration(Integer seconds) {
		if (seconds == null || seconds < 0) {
			return null;
		}
		int minute = seconds / 60;
		int second = seconds % 60;
		return minute + "'" + String.format("%02d", second) + "\"";
	}
}
