package com.playlet.internal.service;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.playlet.internal.base.ResponseBase;

@RequestMapping("/api/file")
public interface FileTestService {
	
	@PostMapping("/upload")
	ResponseBase upload(MultipartFile file);

}
