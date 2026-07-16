package com.playlet.internal.service;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.drama.AddDramaAssetQuery;

import io.swagger.annotations.Api;

@RequestMapping("/dramaAsset")
@Api(value = "短剧剧集管理", tags = "短剧剧集管理")
public interface DramaAssetService {
	
	/**
	 * @category 上传短剧视频
	 * @param entity
	 * @param file
	 * @return
	 */
	@PostMapping("/release")
	ResponseBase addDrama(AddDramaAssetQuery entity,MultipartFile file);

}
