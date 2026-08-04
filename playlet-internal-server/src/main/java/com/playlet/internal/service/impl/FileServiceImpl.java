package com.playlet.internal.service.impl;

import com.playlet.internal.service.FileService;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.utils.QiniuUploadUtils;

@Slf4j
@RestController
@Transactional
@CrossOrigin
public class FileServiceImpl extends BaseApiService implements FileService {

	@Override
	public ResponseBase upload(MultipartFile file) {
		String key = QiniuUploadUtils.uploadFile(file, "VD_1/cover/");
		log.info("file uploaded key={}", key);
		return setResultSuccess(key, I18nUtil.getMessage("base_success"));
	}

}
