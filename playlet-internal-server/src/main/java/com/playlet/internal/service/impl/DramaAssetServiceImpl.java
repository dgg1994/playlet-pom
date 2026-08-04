package com.playlet.internal.service.impl;


import lombok.extern.slf4j.Slf4j;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.enums.VideoDefinitionEnums;
import com.playlet.internal.query.drama.AddDramaAssetQuery;
import com.playlet.internal.service.DramaAssetService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.QiniuUploadUtils;

@RestController
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
@Slf4j
public class DramaAssetServiceImpl extends BaseApiService implements DramaAssetService{
	
	@Autowired
	private DramaDao dramaDao;
	
	@Autowired
	private DramaAssetDao dramaAssetDao;

	@Override
	public ResponseBase addDrama(@Valid AddDramaAssetQuery createPay, MultipartFile file) {
		try {
			DramaEntity entity = dramaDao.selectById(createPay.getDramaId());
			if(entity == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			if(file == null) {
				return setResultError(I18nUtil.getMessage("video_not_null"));
			}
			String path = String.format(Constants.VIDEO_UPLOAD_SITE, entity.getId(),createPay.getSetNum());
			String url = QiniuUploadUtils.uploadVideo(file,path);
			// 多码率约定：库内存默认清晰度 key，如 oceans_720.m3u8；播放时再推导 360/480/720/1080
			String newUrl = toDefaultMultiRateM3u8Key(url);
			DramaAssetEntity assetEntity = new DramaAssetEntity();
			assetEntity.setVideoName(file.getOriginalFilename());
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
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
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
