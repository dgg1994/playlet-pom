package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.drama.RankListEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
