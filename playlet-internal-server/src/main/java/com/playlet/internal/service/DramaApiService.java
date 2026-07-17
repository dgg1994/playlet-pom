package com.playlet.internal.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.drama.RecommendDramaQuery;

import io.swagger.annotations.Api;

@RequestMapping("/api/drama")
@Api(value = "短剧接口", tags = "短剧接口")
public interface DramaApiService {
	
	/**
	 * @category 推荐视频
	 * @param entity
	 * @return
	 */
	@PostMapping("/recommend")
	ResponseBase recommend(RecommendDramaQuery entity);
	
	/**
	 * @category 视频播放上报
	 * @param entity
	 * @return
	 */
	@GetMapping("/playVideo")
	ResponseBase playVideo(Integer id);

}
