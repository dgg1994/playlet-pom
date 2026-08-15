package com.playlet.internal.service.impl;

import com.playlet.internal.api.response.DramaAssetBatchShelfFailItem;
import com.playlet.internal.api.response.DramaAssetBatchShelfRespEntity;
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
import com.playlet.internal.query.drama.DramaAssetBatchShelfQuery;
import com.playlet.internal.query.drama.DramaAssetShelfQuery;
import com.playlet.internal.query.drama.DramaVideoUploadTokenQuery;
import com.playlet.internal.service.DramaAssetAuditService;
import com.playlet.internal.service.DramaAssetDurationService;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

	@Autowired
	private DramaAssetDurationService dramaAssetDurationService;

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
				// 对原始上传文件拉时长，勿用转码后的 m3u8 key
				dramaAssetDurationService.fillDurationFromAvinfo(existing.getId(), key);

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
			dramaAssetDurationService.fillDurationFromAvinfo(assetEntity.getId(), key);
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
			String err = shelfOne(query.getAssetId(), uid, true);
			if (err != null) {
				return setResultError(err);
			}
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
			String err = shelfOne(query.getAssetId(), uid, false);
			if (err != null) {
				return setResultError(err);
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("drama asset unshelf error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase batchShelf(@Valid @RequestBody DramaAssetBatchShelfQuery query, HttpServletRequest request) {
		return batchShelfOrUnshelf(query, request, true);
	}

	@Override
	public ResponseBase batchUnshelf(@Valid @RequestBody DramaAssetBatchShelfQuery query, HttpServletRequest request) {
		return batchShelfOrUnshelf(query, request, false);
	}

	/** 批量上架/下架：逐条处理，剧维度去重后统一 sync */
	private ResponseBase batchShelfOrUnshelf(DramaAssetBatchShelfQuery query, HttpServletRequest request,
			boolean shelfOn) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			if (uid == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			List<Integer> assetIds = normalizeAssetIds(query.getAssetIds());
			if (assetIds.isEmpty()) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			if (assetIds.size() > Constants.MAX_PAGESIZE) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			DramaAssetBatchShelfRespEntity resp = new DramaAssetBatchShelfRespEntity();
			Set<Integer> dramaIdsToSync = new HashSet<>();
			for (Integer assetId : assetIds) {
				ShelfOneResult result = shelfOneWithDrama(assetId, uid, shelfOn);
				if (result.errorMsg != null) {
					DramaAssetBatchShelfFailItem fail = new DramaAssetBatchShelfFailItem();
					fail.setAssetId(assetId);
					fail.setMessage(result.errorMsg);
					resp.getFailItems().add(fail);
					continue;
				}
				resp.getSuccessIds().add(assetId);
				if (result.dramaId != null) {
					dramaIdsToSync.add(result.dramaId);
				}
			}
			for (Integer dramaId : dramaIdsToSync) {
				dramaAuditService.syncDramaShelfByEpisodes(dramaId);
			}
			resp.setSuccessCount(resp.getSuccessIds().size());
			resp.setFailCount(resp.getFailItems().size());
			log.info("drama asset batch {} uid={} success={} fail={}",
					shelfOn ? "shelf" : "unshelf", uid, resp.getSuccessCount(), resp.getFailCount());
			return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("drama asset batch {} error", shelfOn ? "shelf" : "unshelf", e);
			throw new RuntimeException(e);
		}
	}

	/** 单集上架/下架并立刻 sync 整剧（单接口用） */
	private String shelfOne(Integer assetId, Integer uid, boolean shelfOn) {
		ShelfOneResult result = shelfOneWithDrama(assetId, uid, shelfOn);
		if (result.errorMsg != null) {
			return result.errorMsg;
		}
		if (result.dramaId != null) {
			dramaAuditService.syncDramaShelfByEpisodes(result.dramaId);
		}
		return null;
	}

	/**
	 * 单集上下架核心逻辑；成功时返回 dramaId 供批量去重 sync，失败返回 errorMsg。
	 */
	private ShelfOneResult shelfOneWithDrama(Integer assetId, Integer uid, boolean shelfOn) {
		ShelfOneResult result = new ShelfOneResult();
		if (assetId == null) {
			result.errorMsg = I18nUtil.getMessage("base_error");
			return result;
		}
		DramaAssetEntity asset = dramaAssetDao.selectById(assetId);
		if (asset == null || DeleteStateEnum.DELETE.getIndex().equals(asset.getDeleteState())) {
			result.errorMsg = I18nUtil.getMessage("base_data_null");
			return result;
		}
		DramaEntity drama = dramaDao.selectById(asset.getDramaId());
		if (drama == null || DeleteStateEnum.DELETE.getIndex().equals(drama.getDeleteState())) {
			result.errorMsg = I18nUtil.getMessage("drama_null");
			return result;
		}
		ResponseBase ownerErr = assertOwner(drama, uid);
		if (ownerErr != null) {
			result.errorMsg = ownerErr.getMsg();
			return result;
		}
		Date now = new Date();
		if (shelfOn) {
			if (!isApproved(asset.getAuditStatus()) || !isApproved(drama.getAuditStatus())) {
				result.errorMsg = I18nUtil.getMessage("base_error");
				return result;
			}
			// 已上架：幂等成功
			if (asset.getShelfStatus() == null
					|| !asset.getShelfStatus().equals(DramaAssetShelfStatusEnums.ON.getCode())) {
				asset.setShelfStatus(DramaAssetShelfStatusEnums.ON.getCode());
				asset.setVideoStatus(PublicEnums.ONE.getIndex());
				asset.setShelfTime(now);
				asset.setGmtModified(now);
				dramaAssetDao.updateById(asset);
			}
		} else {
			// 已下架：幂等成功，仍需 sync
			if (asset.getShelfStatus() != null
					&& !asset.getShelfStatus().equals(DramaAssetShelfStatusEnums.OFF.getCode())) {
				asset.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
				asset.setVideoStatus(PublicEnums.ZERO.getIndex());
				asset.setShelfTime(null);
				asset.setGmtModified(now);
				dramaAssetDao.updateById(asset);
			}
		}
		result.dramaId = drama.getId();
		return result;
	}

	/** 去重并去掉 null */
	private static List<Integer> normalizeAssetIds(List<Integer> raw) {
		if (raw == null || raw.isEmpty()) {
			return new ArrayList<>();
		}
		Set<Integer> uniq = new LinkedHashSet<>();
		for (Integer id : raw) {
			if (id != null) {
				uniq.add(id);
			}
		}
		return new ArrayList<>(uniq);
	}

	private static class ShelfOneResult {
		private Integer dramaId;
		private String errorMsg;
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
