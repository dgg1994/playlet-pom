package com.playlet.internal.service.impl;

import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.enums.DramaAssetAuditStatusEnums;
import com.playlet.internal.enums.DramaAssetShelfStatusEnums;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.enums.VideoDefinitionEnums;
import com.playlet.internal.query.drama.AddDramaAssetQuery;
import com.playlet.internal.query.drama.DramaVideoUploadTokenQuery;
import com.playlet.internal.api.response.DramaAssetReleaseRespEntity;
import com.playlet.internal.api.response.DramaVideoUploadRespEntity;
import com.playlet.internal.service.DramaAssetService;
import com.playlet.internal.service.DramaAssetAuditService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.QiniuUploadUtils;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
@Slf4j
public class DramaAssetServiceImpl extends BaseApiService implements DramaAssetService{
	
	@Autowired
	private DramaDao dramaDao;
	
	@Autowired
	private DramaAssetDao dramaAssetDao;

	@Autowired
	private MediaUrlService mediaUrlService;

	@Autowired
	private DramaAssetAuditService dramaAssetAuditService;

	@Override
	public ResponseBase uploadToken(@Valid @RequestBody DramaVideoUploadTokenQuery query) {
		try {
			DramaEntity entity = dramaDao.selectById(query.getDramaId());
			if (entity == null || DeleteStateEnum.DELETE.getIndex().equals(entity.getDeleteState())) {
				return setResultError(I18nUtil.getMessage("drama_null"));
			}
			DramaVideoUploadRespEntity cred = QiniuUploadUtils.createVideoUploadCredential(
					query.getDramaId(), query.getSetNum(), query.getExt());
			return setResultSuccess(cred, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase addDrama(@Valid @RequestBody AddDramaAssetQuery createPay) {
		try {
			DramaEntity entity = dramaDao.selectById(createPay.getDramaId());
			if (entity == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			String key = QiniuUploadUtils.extractKey(createPay.getKey());
			if (StringUtils.isEmpty(key)) {
				return setResultError(I18nUtil.getMessage("video_not_null"));
			}
			String prefix = QiniuUploadUtils.videoKeyPrefix(entity.getId(), createPay.getSetNum());
			if (!key.startsWith(prefix)) {
				return setResultError(I18nUtil.getMessage("purview_error_null"));
			}
			if (!QiniuUploadUtils.exists(key)) {
				return setResultError(I18nUtil.getMessage("video_not_null"));
			}
			// 多码率约定：库内存默认清晰度 key，如 oceans_720.m3u8；播放时再推导 360/480/720/1080
			String newUrl = toDefaultMultiRateM3u8Key(key);
			String videoName = StringUtils.isEmpty(createPay.getVideoName())
					? key.substring(key.lastIndexOf('/') + 1) : createPay.getVideoName();

			DramaAssetEntity existing = dramaAssetDao.findByDramaIdAndSetNum(entity.getId(), createPay.getSetNum());
			if (existing != null) {
				// 仅驳回后允许同集覆盖重传；审核中/已通过不可重复登记
				if (existing.getAuditStatus() == null
						|| !existing.getAuditStatus().equals(DramaAssetAuditStatusEnums.REJECTED.getCode())) {
					return setResultError(I18nUtil.getMessage("base_info_exist"));
				}
				existing.setVideoName(videoName);
				existing.setRemarkInfo(createPay.getRemarkInfo());
				existing.setVideoType(entity.getVideoType());
				existing.setVideoUrl(newUrl);
				existing.setBelongUser(entity.getBelongUser());
				existing.setVideoStatus(PublicEnums.ZERO.getIndex());
				existing.setAuditStatus(DramaAssetAuditStatusEnums.UNDER_REVIEW.getCode());
				existing.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
				existing.setAuditRejectReason(null);
				existing.setAuditPassTime(null);
				existing.setShelfTime(null);
				GenericityUtil.updateDate(existing);
				dramaAssetDao.updateById(existing);
				dramaAssetAuditService.initAuditStepsOnRelease(existing.getId(), entity.getId());

				DramaAssetReleaseRespEntity data = new DramaAssetReleaseRespEntity();
				data.setId(existing.getId());
				data.setKey(newUrl);
				data.setVideoUrl(mediaUrlService.sign(newUrl));
				return setResultSuccess(data, I18nUtil.getMessage("base_success"));
			}

			DramaAssetEntity assetEntity = new DramaAssetEntity();
			assetEntity.setVideoName(videoName);
			assetEntity.setBelongUser(entity.getBelongUser());
			assetEntity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			assetEntity.setDramaId(entity.getId());
			assetEntity.setRemarkInfo(createPay.getRemarkInfo());
			assetEntity.setSetNum(createPay.getSetNum());
			assetEntity.setVideoType(entity.getVideoType());
			assetEntity.setVideoUrl(newUrl);
			assetEntity.setVideoStatus(PublicEnums.ZERO.getIndex());
			assetEntity.setAuditStatus(DramaAssetAuditStatusEnums.UNDER_REVIEW.getCode());
			assetEntity.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
			assetEntity.setAuditRejectReason(null);
			assetEntity.setAuditPassTime(null);
			assetEntity.setShelfTime(null);
			GenericityUtil.setDate(assetEntity);
			dramaAssetDao.insert(assetEntity);
			dramaAssetAuditService.initAuditStepsOnRelease(assetEntity.getId(), entity.getId());
			DramaAssetReleaseRespEntity data = new DramaAssetReleaseRespEntity();
			data.setId(assetEntity.getId());
			data.setKey(newUrl);
			data.setVideoUrl(mediaUrlService.sign(newUrl));
			return setResultSuccess(data, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase delDrama(@RequestParam("id") Long id) {
		try {
			DramaAssetEntity entity = dramaAssetDao.selectById(id);
			if (entity == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			dramaAssetDao.deleteById(id);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {}
		return null;
	}

	/**
	 * 上传源片后，DB 存默认清晰度 m3u8 key：
	 * oceans.mp4 / oceans.m3u8 -> oceans_720.m3u8
	 * oceans_720.m3u8 已带码率后缀则规范为默认清晰度
	 */
	private String toDefaultMultiRateM3u8Key(String uploadedKey) {
		String m3u8Key = QiniuUploadUtils.replaceFileExtension(uploadedKey, Constants.M3U8);
		if (m3u8Key == null || m3u8Key.isEmpty()) {
			return m3u8Key;
		}
		String lower = m3u8Key.toLowerCase();
		if (!lower.endsWith(".m3u8")) {
			return m3u8Key;
		}
		String withoutExt = m3u8Key.substring(0, m3u8Key.length() - ".m3u8".length());
		if (VideoDefinitionEnums.hasDefinitionSuffix(withoutExt)) {
			return VideoDefinitionEnums.replaceDefinitionSuffix(withoutExt, VideoDefinitionEnums.DEFAULT) + ".m3u8";
		}
		return VideoDefinitionEnums.DEFAULT.toM3u8Key(withoutExt);
	}

}
