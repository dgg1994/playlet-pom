package com.playlet.oversea.service;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.playlet.oversea.base.ResponseBase;

@RequestMapping("/file")
public interface FileService {
	
	@PostMapping("/upload")
	ResponseBase upload(MultipartFile file);

}
