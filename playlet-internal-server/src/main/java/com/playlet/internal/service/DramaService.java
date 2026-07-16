package com.playlet.internal.service;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.drama.AddDramaQuery;

import io.swagger.annotations.Api;

@RequestMapping("/drama")
@Api(value = "短剧管理", tags = "短剧管理")
public interface DramaService {
	
	/**
	 * @category 短剧基础信息发布
	 * @param entity
	 * @param file
	 * @return
	 */
	@PostMapping("/release")
	ResponseBase addDrama(AddDramaQuery entity,MultipartFile file);

}
