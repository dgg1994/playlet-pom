package com.playlet.oversea.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.query.drama.BatchVideoDownloadQuery;
import com.playlet.oversea.query.drama.RecommendDramaQuery;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RequestMapping("/api/drama")
@Api(value = "短剧接口", tags = "短剧接口")
public interface DramaApiService {
	
	/**
	 * @category 推荐视频（同 seed 内分页顺序稳定，避免翻页重复）
	 * @param entity 首页可不传 seed；响应会返回 seed，后续翻页原样回传
	 * @return
	 */
	@PostMapping("/recommend")
	ResponseBase recommend(RecommendDramaQuery entity, HttpServletRequest request);
	
	/**
	 * @category 视频播放上报
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
	ResponseBase selections(Integer id, HttpServletRequest request);
	
	/**
	 * @category 获取视频播放地址（多码率）
	 * @param id drama_asset.id
	 * @return data.streams 为 360/480/720/1080 各路签名地址；默认播 defaultDefinition
	 */
	@GetMapping("/getVideoUrl")
	ResponseBase getVideoUrl(Integer id);

	/**
	 * @category 批量获取指定分辨率视频下载地址
	 * @return data 为数组，每项含 assetId + path + downloadUrl
	 */
	@PostMapping("/getVideoDownloadUrl")
	@ApiOperation(value = "批量获取指定分辨率下载地址",
			notes = "body: {\"ids\":[1,2,3],\"definition\":\"720\"}；命名约定 {prefix}_{definition}.mp4")
	ResponseBase getVideoDownloadUrl(BatchVideoDownloadQuery query);
	
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
	
	/**
	 * @category 获取视频详情
	 * @param id
	 * @return
	 */
	@GetMapping("/getVideoInfo")
	ResponseBase getVideoInfo(Integer id,HttpServletRequest  request);
	
}
