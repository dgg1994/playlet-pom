package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.drama.UserDramaCollectEntity;
import com.playlet.internal.entity.drama.UserDramaLikeEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * C端收藏/点赞：网关 /china/admin/api/theater/**
 */
@RequestMapping("/api/theater")
@Api(value = "剧场互动", tags = "剧场互动")
public interface UserInteractService {

	@PostMapping("/collect/add")
	@ApiImplicitParam(name = "dramaId", value = "短剧ID", required = true, dataType = "int", paramType = "query")
	@ApiOperation(value = "收藏短剧", notes = "需登录；幂等")
	ResponseBase collectAdd(Integer dramaId, HttpServletRequest request);

	@PostMapping("/collect/cancel")
	@ApiImplicitParam(name = "dramaId", value = "短剧ID", required = true, dataType = "int", paramType = "query")
	@ApiOperation(value = "取消收藏", notes = "需登录；幂等")
	ResponseBase collectCancel(Integer dramaId, HttpServletRequest request);

	@GetMapping("/collect/list")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "pageNumber", value = "页码", required = false, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "pageSize", value = "每页", required = false, dataType = "int", paramType = "query")
	})
	@ApiOperation(value = "我的收藏列表", notes = "需登录")
	ResponseBase collectList(UserDramaCollectEntity entity, HttpServletRequest request);

	@PostMapping("/like/drama")
	@ApiImplicitParam(name = "dramaId", value = "短剧ID", required = true, dataType = "int", paramType = "query")
	@ApiOperation(value = "整剧点赞", notes = "需登录；计入 drama.like_score")
	ResponseBase likeDrama(Integer dramaId, HttpServletRequest request);

	@PostMapping("/like/drama/cancel")
	@ApiImplicitParam(name = "dramaId", value = "短剧ID", required = true, dataType = "int", paramType = "query")
	@ApiOperation(value = "取消整剧点赞", notes = "需登录")
	ResponseBase likeDramaCancel(Integer dramaId, HttpServletRequest request);

	@PostMapping("/like/episode")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "dramaId", value = "短剧ID", required = true, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "episodeId", value = "分集ID", required = true, dataType = "string", paramType = "query")
	})
	@ApiOperation(value = "单集点赞", notes = "需登录；同时计入短剧总赞")
	ResponseBase likeEpisode(Integer dramaId, String episodeId, HttpServletRequest request);

	@PostMapping("/like/episode/cancel")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "dramaId", value = "短剧ID", required = true, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "episodeId", value = "分集ID", required = true, dataType = "string", paramType = "query")
	})
	@ApiOperation(value = "取消单集点赞", notes = "需登录；短剧总赞-1")
	ResponseBase likeEpisodeCancel(Integer dramaId, String episodeId, HttpServletRequest request);

	@GetMapping("/like/list")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "pageNumber", value = "页码", required = false, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "pageSize", value = "每页", required = false, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "likeType", value = "1整剧 2单集，不传查全部", required = false, dataType = "int", paramType = "query")
	})
	@ApiOperation(value = "我的点赞列表", notes = "需登录；含整剧/单集点赞")
	ResponseBase likeList(UserDramaLikeEntity entity, HttpServletRequest request);

	@PostMapping("/share/drama")
	@ApiImplicitParam(name = "dramaId", value = "短剧ID", required = true, dataType = "int", paramType = "query")
	@ApiOperation(value = "分享上报", notes = "需登录；同一用户同一剧 60 秒内只计一次")
	ResponseBase shareDrama(Integer dramaId, HttpServletRequest request);
}
