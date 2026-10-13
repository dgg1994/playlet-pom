package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * C端内容分享素材：网关 /entrance/api/share/**
 */
@RequestMapping("/api/share")
@Api(value = "内容分享", tags = "内容分享")
public interface ShareApiService {

	@GetMapping("/info")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "dramaId", value = "短剧ID，与 videoId 至少传一个", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "videoId", value = "集ID，播放页建议传；非法时降级为整剧", dataType = "int", paramType = "query")
	})
	@ApiOperation(value = "分享素材", notes = "可不登录；返回 title/desc/coverUrl/shareUrl，供播放页分享面板使用")
	ResponseBase info(@RequestParam(value = "dramaId", required = false) Integer dramaId,
			@RequestParam(value = "videoId", required = false) Integer videoId);
}
