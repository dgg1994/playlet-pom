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
	@GetMapping("/playVideoReport")
	ResponseBase playVideoReport(Integer id);
	
	/**
	 * @category 选集视频列表
	 * @param id
	 * @return
	 */
	@GetMapping("/selections")
	ResponseBase selections(Integer id);
	
	/**
	 * @category 获取视频播放地址
	 * @param id
	 * @return
	 */
	@GetMapping("/getVideoUrl")
	ResponseBase getVideoUrl(Integer id);
	
	/**
	 * @category 作品详情
	 * @param id
	 * @return
	 */
	@GetMapping("/workInfo")
	ResponseBase workInfo(Integer id);
	
	/**
	 * @category 相关作品
	 * @param id
	 * @return
	 */
	@GetMapping("/relatedWork")
	ResponseBase relatedWork(Integer id);
	
	/**
	 * @category 开始播放查询第一集播放地址
	 * @param id
	 * @return
	 */
	@GetMapping("/playVideo")
	ResponseBase playVideo(Integer id);

}
