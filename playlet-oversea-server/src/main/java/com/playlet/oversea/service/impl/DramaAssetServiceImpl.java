package com.playlet.oversea.service.impl;

import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.dao.drama.DramaAssetDao;
import com.playlet.oversea.dao.drama.DramaDao;
import com.playlet.oversea.entity.drama.DramaAssetEntity;
import com.playlet.oversea.entity.drama.DramaEntity;
import com.playlet.oversea.enums.DeleteStateEnum;
import com.playlet.oversea.enums.PublicEnums;
import com.playlet.oversea.enums.VideoDefinitionEnums;
import com.playlet.oversea.query.drama.AddDramaAssetQuery;
import com.playlet.oversea.query.drama.DramaVideoUploadTokenQuery;
import com.playlet.oversea.response.drama.DramaAssetReleaseResp;
import com.playlet.oversea.response.drama.DramaVideoUploadResp;
import com.playlet.oversea.service.DramaAssetService;
import com.playlet.oversea.service.MediaUrlService;
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

	@Override
	public ResponseBase uploadToken(@Valid @RequestBody DramaVideoUploadTokenQuery query) {
		try {
			DramaEntity entity = dramaDao.selectById(query.getDramaId());
			if (entity == null || DeleteStateEnum.DELETE.getIndex().equals(entity.getDeleteState())) {
				return setResultError(I18nUtil.getMessage("drama_null"));
			}
			DramaVideoUploadResp cred = QiniuUploadUtils.createVideoUploadCredential(
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
			DramaAssetEntity assetEntity = new DramaAssetEntity();
			assetEntity.setVideoName(StringUtils.isEmpty(createPay.getVideoName())
					? key.substring(key.lastIndexOf('/') + 1) : createPay.getVideoName());
			assetEntity.setBelongUser(entity.getBelongUser());
			assetEntity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			assetEntity.setDramaId(entity.getId());
			assetEntity.setRemarkInfo(createPay.getRemarkInfo());
			assetEntity.setSetNum(createPay.getSetNum());
			assetEntity.setVideoType(entity.getVideoType());
			assetEntity.setVideoUrl(newUrl);
			assetEntity.setVideoStatus(PublicEnums.ONE.getIndex());
			GenericityUtil.setDate(assetEntity);
			dramaAssetDao.insert(assetEntity);
			DramaAssetReleaseResp data = new DramaAssetReleaseResp();
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
