package com.playlet.oversea.service.impl;

import com.playlet.oversea.api.response.DramaAssetReleaseRespEntity;
import com.playlet.oversea.api.response.DramaVideoUploadRespEntity;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.dao.drama.DramaAssetDao;
import com.playlet.oversea.dao.drama.DramaDao;
import com.playlet.oversea.entity.drama.DramaAssetEntity;
import com.playlet.oversea.entity.drama.DramaEntity;
import com.playlet.oversea.enums.*;
import com.playlet.oversea.query.drama.*;
import com.playlet.oversea.service.*;
import com.playlet.oversea.utils.GenericityUtil;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.QiniuUploadUtils;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
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
	public ResponseBase addDrama(@Valid @RequestBody BatchDramaAssetReleaseQuery query) {
		try {
			DramaEntity drama = dramaDao.selectById(query.getDramaId());
			if (drama == null || DeleteStateEnum.DELETE.getIndex().equals(drama.getDeleteState())) {
				return setResultError(I18nUtil.getMessage("drama_null"));
			}
			List<BatchDramaAssetEpisodeItemQuery> episodes = query.getEpisodes();
			String batchErr = validateBatchEpisodes(episodes);
			if (batchErr != null) {
				return setResultError(batchErr);
			}
			// 两阶段改集序：先写负 assetId 临时集号，避免同批互换时撞唯一集号
			for (BatchDramaAssetEpisodeItemQuery item : episodes) {
				if (item.getAssetId() == null) {
					continue;
				}
				DramaAssetEntity asset = loadDramaAsset(item.getAssetId(), drama.getId());
				if (asset == null) {
					return setResultError(I18nUtil.getMessage("base_data_null"));
				}
				asset.setSetNum(-item.getAssetId());
				GenericityUtil.updateDate(asset);
				dramaAssetDao.updateById(asset);
			}
			List<DramaAssetReleaseRespEntity> results = new ArrayList<>();
			for (BatchDramaAssetEpisodeItemQuery item : episodes) {
				EpisodeProcessResult outcome = processBatchEpisode(drama, item);
				if (!outcome.isOk()) {
					log.warn("drama asset release item failed dramaId={} assetId={} setNum={} err={}",
							drama.getId(), item.getAssetId(), item.getSetNum(), outcome.getErrorMsg());
					return setResultError(outcome.getErrorMsg());
				}
				results.add(outcome.getResp());
			}
			log.info("drama asset release dramaId={} size={}", drama.getId(), results.size());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("drama asset release error dramaId={}", query == null ? null : query.getDramaId(), e);
			throw new RuntimeException(e);
		}
	}

	/** 校验批量条目：集号非空、同批不重复、新集必须有 key。 */
	private String validateBatchEpisodes(List<BatchDramaAssetEpisodeItemQuery> episodes) {
		Set<Integer> setNums = new HashSet<>();
		for (BatchDramaAssetEpisodeItemQuery item : episodes) {
			if (item.getSetNum() == null) {
				return I18nUtil.getMessage("base_error");
			}
			if (!setNums.add(item.getSetNum())) {
				return I18nUtil.getMessage("base_info_exist");
			}
			boolean hasKey = !StringUtils.isEmpty(item.getKey());
			if (item.getAssetId() == null && !hasKey) {
				return I18nUtil.getMessage("video_not_null");
			}
		}
		return null;
	}

	/**
	 * 处理单条批量剧集：历史集仅改序；新集登记；带 key 的驳回稿重传。
	 */
	private EpisodeProcessResult processBatchEpisode(DramaEntity drama,
			BatchDramaAssetEpisodeItemQuery item) throws Exception {
		boolean hasKey = !StringUtils.isEmpty(item.getKey());
		if (item.getAssetId() != null) {
			DramaAssetEntity asset = loadDramaAsset(item.getAssetId(), drama.getId());
			if (asset == null) {
				return EpisodeProcessResult.fail(I18nUtil.getMessage("base_data_null"));
			}
			if (hasKey) {
				if (asset.getAuditStatus() == null
						|| asset.getAuditStatus() != DramaAssetAuditStatusEnums.REJECTED.getCode()) {
					return EpisodeProcessResult.fail(I18nUtil.getMessage("base_info_exist"));
				}
				String key = validateUploadedVideo(item.getKey());
				if (key == null) {
					return EpisodeProcessResult.fail(I18nUtil.getMessage("video_not_null"));
				}
				// 不再校验 key 中 EP_{n} 与 setNum 一致，允许批量改序后沿用原 uploadToken 路径
				// if (!hasValidVideoPrefix(drama.getId(), item.getSetNum(), key)) {
				// 	return EpisodeProcessResult.fail(I18nUtil.getMessage("purview_error_null"));
				// }
				String videoUrl = toDefaultMultiRateM3u8Key(key);
				String videoName = resolveVideoName(item.getVideoName(), key);
				return EpisodeProcessResult.ok(resetRejectedAssetEntity(asset, drama, item.getSetNum(),
						item.getRemarkInfo(), videoName, videoUrl, key));
			}
			// 历史集：仅同步集序/备注，不动 video_url
			asset.setSetNum(item.getSetNum());
			if (item.getRemarkInfo() != null) {
				asset.setRemarkInfo(item.getRemarkInfo());
			}
			GenericityUtil.updateDate(asset);
			dramaAssetDao.updateById(asset);
			return EpisodeProcessResult.ok(buildReleaseResp(asset.getId(), asset.getVideoUrl()));
		}
		// 新上传
		String key = validateUploadedVideo(item.getKey());
		if (key == null) {
			return EpisodeProcessResult.fail(I18nUtil.getMessage("video_not_null"));
		}
		// 不再校验 key 中 EP_{n} 与 setNum 一致，允许批量改序后沿用原 uploadToken 路径
		// if (!hasValidVideoPrefix(drama.getId(), item.getSetNum(), key)) {
		// 	return EpisodeProcessResult.fail(I18nUtil.getMessage("purview_error_null"));
		// }
		String videoUrl = toDefaultMultiRateM3u8Key(key);
		String videoName = resolveVideoName(item.getVideoName(), key);
		DramaAssetEntity existing = dramaAssetDao.findByDramaIdAndSetNum(drama.getId(), item.getSetNum());
		if (existing != null) {
			if (existing.getAuditStatus() == null
					|| !existing.getAuditStatus().equals(DramaAssetAuditStatusEnums.REJECTED.getCode())) {
				return EpisodeProcessResult.fail(I18nUtil.getMessage("base_info_exist"));
			}
			return EpisodeProcessResult.ok(resetRejectedAssetEntity(existing, drama, item.getSetNum(),
					item.getRemarkInfo(), videoName, videoUrl, key));
		}
		return EpisodeProcessResult.ok(insertNewAssetEntity(drama, item.getSetNum(), item.getRemarkInfo(),
				videoName, videoUrl, key));
	}

	/** 单条剧集处理结果，避免 null 语义不清。 */
	private static class EpisodeProcessResult {
		private final DramaAssetReleaseRespEntity resp;
		private final String errorMsg;

		private EpisodeProcessResult(DramaAssetReleaseRespEntity resp, String errorMsg) {
			this.resp = resp;
			this.errorMsg = errorMsg;
		}

		static EpisodeProcessResult ok(DramaAssetReleaseRespEntity resp) {
			return new EpisodeProcessResult(resp, null);
		}

		static EpisodeProcessResult fail(String errorMsg) {
			return new EpisodeProcessResult(null, errorMsg);
		}

		boolean isOk() {
			return errorMsg == null;
		}

		DramaAssetReleaseRespEntity getResp() {
			return resp;
		}

		String getErrorMsg() {
			return errorMsg;
		}
	}

	private DramaAssetEntity loadDramaAsset(Integer assetId, Integer dramaId) {
		DramaAssetEntity asset = dramaAssetDao.selectById(assetId);
		if (asset == null || DeleteStateEnum.DELETE.getIndex().equals(asset.getDeleteState())) {
			return null;
		}
		if (asset.getDramaId() == null || !asset.getDramaId().equals(dramaId)) {
			return null;
		}
		return asset;
	}

	private DramaAssetReleaseRespEntity insertNewAssetEntity(DramaEntity drama, Integer setNum, String remarkInfo,
			String videoName, String videoUrl, String sourceKey) throws Exception {
		DramaAssetEntity assetEntity = new DramaAssetEntity();
		assetEntity.setVideoName(videoName);
		assetEntity.setBelongUser(drama.getBelongUser());
		assetEntity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
		assetEntity.setDramaId(drama.getId());
		assetEntity.setRemarkInfo(remarkInfo);
		assetEntity.setSetNum(setNum);
		assetEntity.setVideoType(drama.getVideoType());
		assetEntity.setVideoUrl(videoUrl);
		assetEntity.setVideoStatus(PublicEnums.ZERO.getIndex());
		assetEntity.setAuditStatus(DramaAssetAuditStatusEnums.UNDER_REVIEW.getCode());
		assetEntity.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
		assetEntity.setAuditRejectReason(null);
		assetEntity.setAuditPassTime(null);
		assetEntity.setShelfTime(null);
		GenericityUtil.setDate(assetEntity);
		dramaAssetDao.insert(assetEntity);
		dramaAssetAuditService.initAuditStepsOnRelease(assetEntity.getId(), drama.getId());
		dramaAssetDurationService.fillDurationFromAvinfo(assetEntity.getId(), sourceKey);
		return buildReleaseResp(assetEntity.getId(), videoUrl);
	}

	private DramaAssetReleaseRespEntity resetRejectedAssetEntity(DramaAssetEntity asset, DramaEntity drama,
			Integer setNum, String remarkInfo, String videoName, String videoUrl, String sourceKey) throws Exception {
		asset.setSetNum(setNum);
		asset.setVideoName(videoName);
		asset.setRemarkInfo(remarkInfo);
		asset.setVideoType(drama.getVideoType());
		asset.setVideoUrl(videoUrl);
		asset.setBelongUser(drama.getBelongUser());
		asset.setVideoStatus(PublicEnums.ZERO.getIndex());
		asset.setAuditStatus(DramaAssetAuditStatusEnums.UNDER_REVIEW.getCode());
		asset.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
		asset.setAuditRejectReason(null);
		asset.setAuditPassTime(null);
		asset.setShelfTime(null);
		asset.setAppealStatus(DramaAppealStatusEnums.NONE.getCode());
		asset.setAppealReason(null);
		asset.setAppealTime(null);
		GenericityUtil.updateDate(asset);
		dramaAssetDao.updateById(asset);
		dramaAssetAuditService.initAuditStepsOnRelease(asset.getId(), drama.getId());
		dramaAssetDurationService.fillDurationFromAvinfo(asset.getId(), sourceKey);
		return buildReleaseResp(asset.getId(), videoUrl);
	}

	@Override
	public ResponseBase updateDrama(@Valid @RequestBody UpdateDramaAssetQuery query) {
		try {
			DramaEntity drama = dramaDao.selectById(query.getDramaId());
			if (drama == null) {
				return setResultError(I18nUtil.getMessage("drama_null"));
			}
			DramaAssetEntity current = dramaAssetDao.selectById(query.getId());
			if (current == null || DeleteStateEnum.DELETE.getIndex().equals(current.getDeleteState())) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			if (!drama.getId().equals(current.getDramaId())) {
				return setResultError(I18nUtil.getMessage("purview_error_null"));
			}
			// 仅允许驳回后修改，避免审核中/已通过稿件被覆盖
			if (current.getAuditStatus() == null
					|| current.getAuditStatus() != DramaAssetAuditStatusEnums.REJECTED.getCode()) {
				return setResultError(I18nUtil.getMessage("base_info_exist"));
			}
			DramaAssetEntity duplicate = dramaAssetDao.findByDramaIdAndSetNum(drama.getId(), query.getSetNum());
			if (duplicate != null && !duplicate.getId().equals(current.getId())) {
				return setResultError(I18nUtil.getMessage("base_info_exist"));
			}
			String key = validateUploadedVideo(query.getKey());
			if (key == null) {
				return setResultError(I18nUtil.getMessage("video_not_null"));
			}
			// 不再校验 key 中 EP_{n} 与 setNum 一致
			// if (!hasValidVideoPrefix(drama.getId(), query.getSetNum(), key)) {
			// 	return setResultError(I18nUtil.getMessage("purview_error_null"));
			// }
			String newUrl = toDefaultMultiRateM3u8Key(key);
			String videoName = resolveVideoName(query.getVideoName(), key);
			DramaAssetReleaseRespEntity data = resetRejectedAssetEntity(current, drama, query.getSetNum(),
					query.getRemarkInfo(), videoName, newUrl, key);
			return setResultSuccess(data, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("update drama asset error", e);
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

	/** 统一校验直传文件：提取 key、确认七牛对象存在。 */
	private String validateUploadedVideo(String rawKey) {
		String key = QiniuUploadUtils.extractKey(rawKey);
		if (StringUtils.isEmpty(key)) {
			return null;
		}
		if (!QiniuUploadUtils.exists(key)) {
			return null;
		}
		return key;
	}

	// /** 只允许提交本剧、本集申请到的七牛 key（EP_{setNum} 须与 setNum 一致）。 */
	// private boolean hasValidVideoPrefix(Integer dramaId, Integer setNum, String key) {
	// 	return key != null && key.startsWith(QiniuUploadUtils.videoKeyPrefix(dramaId, setNum));
	// }

	/** 未传原文件名时，默认取 key 最后一段。 */
	private static String resolveVideoName(String videoName, String key) {
		return StringUtils.isEmpty(videoName) ? key.substring(key.lastIndexOf('/') + 1) : videoName;
	}

	/** 统一 release/update 成功响应。 */
	private DramaAssetReleaseRespEntity buildReleaseResp(Integer id, String key) {
		DramaAssetReleaseRespEntity data = new DramaAssetReleaseRespEntity();
		data.setId(id);
		data.setKey(key);
		data.setVideoUrl(mediaUrlService.sign(key));
		return data;
	}

}
