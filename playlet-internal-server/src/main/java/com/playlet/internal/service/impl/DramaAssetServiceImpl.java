package com.playlet.internal.service.impl;

import com.playlet.internal.api.response.DramaAssetReleaseRespEntity;
import com.playlet.internal.api.response.DramaVideoUploadRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.enums.*;
import com.playlet.internal.query.drama.AddDramaAssetQuery;
import com.playlet.internal.query.drama.DramaAssetAppealQuery;
import com.playlet.internal.query.drama.DramaAssetShelfQuery;
import com.playlet.internal.query.drama.DramaVideoUploadTokenQuery;
import com.playlet.internal.service.DramaAssetAuditService;
import com.playlet.internal.service.DramaAssetService;
import com.playlet.internal.service.DramaAuditService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;

@RestController
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
@Slf4j
public class DramaAssetServiceImpl extends BaseApiService implements DramaAssetService {

	@Autowired
	private DramaDao dramaDao;

	@Autowired
	private DramaAssetDao dramaAssetDao;

	@Autowired
	private MediaUrlService mediaUrlService;

	@Autowired
	private DramaAssetAuditService dramaAssetAuditService;

	@Autowired
	private DramaAuditService dramaAuditService;

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
			String newUrl = toDefaultMultiRateM3u8Key(key);
			String videoName = StringUtils.isEmpty(createPay.getVideoName())
					? key.substring(key.lastIndexOf('/') + 1) : createPay.getVideoName();

			DramaAssetEntity existing = dramaAssetDao.findByDramaIdAndSetNum(entity.getId(), createPay.getSetNum());
			if (existing != null) {
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
				// 驳回重传：清空申诉态，按新稿重新进审
				existing.setAppealStatus(DramaAppealStatusEnums.NONE.getCode());
				existing.setAppealReason(null);
				existing.setAppealTime(null);
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
	public ResponseBase shelf(@Valid @RequestBody DramaAssetShelfQuery query, HttpServletRequest request) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			if (uid == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			DramaAssetEntity asset = dramaAssetDao.selectById(query.getAssetId());
			if (asset == null || DeleteStateEnum.DELETE.getIndex().equals(asset.getDeleteState())) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			DramaEntity drama = dramaDao.selectById(asset.getDramaId());
			if (drama == null || DeleteStateEnum.DELETE.getIndex().equals(drama.getDeleteState())) {
				return setResultError(I18nUtil.getMessage("drama_null"));
			}
			ResponseBase ownerErr = assertOwner(drama, uid);
			if (ownerErr != null) {
				return ownerErr;
			}
			if (!isApproved(asset.getAuditStatus()) || !isApproved(drama.getAuditStatus())) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			if (asset.getShelfStatus() != null
					&& asset.getShelfStatus().equals(DramaAssetShelfStatusEnums.ON.getCode())) {
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}
			Date now = new Date();
			asset.setShelfStatus(DramaAssetShelfStatusEnums.ON.getCode());
			asset.setVideoStatus(PublicEnums.ONE.getIndex());
			asset.setShelfTime(now);
			asset.setGmtModified(now);
			dramaAssetDao.updateById(asset);
			dramaAuditService.syncDramaShelfByEpisodes(drama.getId());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("drama asset shelf error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase unshelf(@Valid @RequestBody DramaAssetShelfQuery query, HttpServletRequest request) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			if (uid == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			DramaAssetEntity asset = dramaAssetDao.selectById(query.getAssetId());
			if (asset == null || DeleteStateEnum.DELETE.getIndex().equals(asset.getDeleteState())) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			DramaEntity drama = dramaDao.selectById(asset.getDramaId());
			if (drama == null || DeleteStateEnum.DELETE.getIndex().equals(drama.getDeleteState())) {
				return setResultError(I18nUtil.getMessage("drama_null"));
			}
			ResponseBase ownerErr = assertOwner(drama, uid);
			if (ownerErr != null) {
				return ownerErr;
			}
			if (asset.getShelfStatus() == null
					|| asset.getShelfStatus().equals(DramaAssetShelfStatusEnums.OFF.getCode())) {
				dramaAuditService.syncDramaShelfByEpisodes(drama.getId());
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}
			Date now = new Date();
			asset.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
			asset.setVideoStatus(PublicEnums.ZERO.getIndex());
			asset.setShelfTime(null);
			asset.setGmtModified(now);
			dramaAssetDao.updateById(asset);
			dramaAuditService.syncDramaShelfByEpisodes(drama.getId());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("drama asset unshelf error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase appeal(@Valid @RequestBody DramaAssetAppealQuery query, HttpServletRequest request) {
		try {
			Integer uid = SysUserTokenUtil.resolveAdminId(request);
			if (uid == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			DramaAssetEntity asset = dramaAssetDao.selectById(query.getAssetId());
			if (asset == null || DeleteStateEnum.DELETE.getIndex().equals(asset.getDeleteState())) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			DramaEntity drama = dramaDao.selectById(asset.getDramaId());
			if (drama == null || DeleteStateEnum.DELETE.getIndex().equals(drama.getDeleteState())) {
				return setResultError(I18nUtil.getMessage("drama_null"));
			}
			ResponseBase ownerErr = assertOwner(drama, uid);
			if (ownerErr != null) {
				return ownerErr;
			}
			// 仅驳回可申诉
			if (asset.getAuditStatus() == null
					|| !asset.getAuditStatus().equals(DramaAssetAuditStatusEnums.REJECTED.getCode())) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			// 申诉中不可重复提交
			if (DramaAssetAuditStatusEnums.isAppealing(asset.getAuditStatus())
					|| (asset.getAppealStatus() != null
					&& asset.getAppealStatus().equals(DramaAppealStatusEnums.APPEALING.getCode()))) {
				return setResultError(I18nUtil.getMessage("base_info_exist"));
			}
			Date now = new Date();
			asset.setAppealStatus(DramaAppealStatusEnums.APPEALING.getCode());
			asset.setAppealReason(query.getRemark().trim());
			asset.setAppealTime(now);
			asset.setAuditStatus(DramaAssetAuditStatusEnums.APPEALING.getCode());
			asset.setGmtModified(now);
			dramaAssetDao.updateById(asset);
			log.info("drama asset appeal submitted assetId={} dramaId={} uid={}",
					asset.getId(), asset.getDramaId(), uid);
			// 重置 AI/A/B 进入再审，聚合逻辑保持 audit_status=4
			dramaAssetAuditService.initAuditStepsOnRelease(asset.getId(), asset.getDramaId());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("drama asset appeal failed assetId={}", query.getAssetId(), e);
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
			Integer dramaId = entity.getDramaId();
			dramaAssetDao.deleteById(id);
			if (dramaId != null) {
				dramaAuditService.syncDramaShelfByEpisodes(dramaId);
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("del drama asset error", e);
			throw new RuntimeException(e);
		}
	}

	private static ResponseBase assertOwner(DramaEntity drama, Integer uid) {
		if (drama.getBelongUser() != null && !drama.getBelongUser().equals(uid)) {
			return setResultError(I18nUtil.getMessage("purview_error_null"));
		}
		return null;
	}

	private static boolean isApproved(Integer auditStatus) {
		return auditStatus != null && auditStatus.equals(DramaAssetAuditStatusEnums.APPROVED.getCode());
	}

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
