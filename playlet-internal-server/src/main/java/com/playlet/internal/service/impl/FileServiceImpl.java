package com.playlet.internal.service.impl;

import com.playlet.internal.service.FileService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.utils.QiniuUploadUtils;

@Slf4j
@RestController
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
public class FileServiceImpl extends BaseApiService implements FileService {

	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	public ResponseBase upload(MultipartFile file) {
		String key = QiniuUploadUtils.uploadFile(file, "VD_1/cover/");
		log.info("file uploaded key={}", key);
		return setResultSuccess(mediaUrlService.sign(key), I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase uploadKey(MultipartFile file) {
		// 备案材料等场景：返回七牛原始 key，由后端在展示时统一签名，避免库里存过期签名URL
		String key = QiniuUploadUtils.uploadFile(file, "VD_1/material/");
		log.info("file uploaded key={}", key);
		return setResultSuccess(key, I18nUtil.getMessage("base_success"));
	}

}
