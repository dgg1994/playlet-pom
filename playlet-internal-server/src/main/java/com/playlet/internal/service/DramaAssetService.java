package com.playlet.internal.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.drama.AddDramaAssetQuery;
import com.playlet.internal.query.drama.DramaVideoUploadTokenQuery;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RequestMapping("/dramaAsset")
@Api(value = "短剧剧集管理", tags = "短剧剧集管理")
public interface DramaAssetService {

	/**
	 * 获取剧集视频前端直传凭证（key 限定在 VD_{dramaId}/EP_{setNum}/）。
	 */
	@PostMapping("/uploadToken")
	@ApiOperation(value = "获取视频直传凭证", notes = "body: {dramaId, setNum, ext?}。"
			+ "返回 uploadToken/key/domain/expireSeconds/uploadUrl；前端直传七牛后再调 /release。")
	ResponseBase uploadToken(DramaVideoUploadTokenQuery query);
	
	/**
	 * 前端直传成功后登记剧集（纯 JSON，不再接收 MultipartFile）。
	 */
	@PostMapping("/release")
	@ApiOperation(value = "登记剧集视频", notes = "body: {dramaId, setNum, key, videoName?, remarkInfo?}。"
			+ "校验 key 前缀并确认对象存在后写入 drama_asset。")
	ResponseBase addDrama(AddDramaAssetQuery entity);

	/**
	 * 删除剧集视频。
	 */
	@GetMapping("/delete")
	ResponseBase delDrama(Long id);

}
