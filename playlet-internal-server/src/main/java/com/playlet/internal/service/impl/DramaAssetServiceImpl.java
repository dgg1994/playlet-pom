package com.playlet.internal.service.impl;

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
import com.playlet.internal.query.drama.AddDramaAssetQuery;
import com.playlet.internal.service.DramaAssetService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.QiniuUploadUtils;

@RestController
@Transactional
@CrossOrigin
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
			String newUrl = QiniuUploadUtils.replaceFileExtension(url, Constants.M3U8);
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
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

}
