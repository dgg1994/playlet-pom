package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.RankListEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * C端剧场：网关 /china/admin/api/theater/**
 */
@RequestMapping("/api/theater")
@Api(value = "剧场", tags = "剧场")
public interface TheaterService {

	@GetMapping("/home")
	@ApiOperation(value = "剧场首页")
	ResponseBase home();

	@GetMapping("/rankList")
	@ApiOperation(value = "榜单列表")
	ResponseBase rankList();

	@GetMapping("/rank")
	@ApiOperation(value = "榜单分页", notes = "读 rank_list，仅 status=1；data=TheaterRankPageRespEntity")
	ResponseBase rank(String boardCode, RankListEntity entity);

	@GetMapping("/search")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "keyword", value = "关键词（标题/简介/标签）", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "pageNumber", value = "页码", required = false, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "pageSize", value = "每页", required = false, dataType = "int", paramType = "query")
	})
	@ApiOperation(value = "剧场搜索", notes = "模糊搜已上架剧；已登录时写入搜索历史")
	ResponseBase search(String keyword, DramaEntity entity, HttpServletRequest request);

	@GetMapping("/search/history")
	@ApiOperation(value = "搜索历史列表", notes = "需 x-playlet-token")
	ResponseBase searchHistory(HttpServletRequest request);

	@GetMapping("/search/history/delete")
	@ApiImplicitParam(name = "keyword", value = "要删除的关键词", required = true, dataType = "string", paramType = "query")
	@ApiOperation(value = "删除单条搜索历史", notes = "需 x-playlet-token")
	ResponseBase deleteSearchHistory(String keyword, HttpServletRequest request);

	@GetMapping("/search/history/clear")
	@ApiOperation(value = "清空搜索历史", notes = "需 x-playlet-token")
	ResponseBase clearSearchHistory(HttpServletRequest request);
}
