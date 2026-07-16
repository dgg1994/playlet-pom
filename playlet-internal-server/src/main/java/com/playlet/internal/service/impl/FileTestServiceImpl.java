package com.playlet.internal.service.impl;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.service.FileTestService;
import com.playlet.internal.utils.QiniuUploadUtils;

@RestController
@Transactional
@CrossOrigin
public class FileTestServiceImpl extends BaseApiService implements FileTestService{

	@Override
	public ResponseBase upload(MultipartFile file) {
		String url = QiniuUploadUtils.uploadFile(file,"VD_1/cover/");
		System.out.println(url);
		return setResultSuccess(url);
	}

}
