package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.RankListEntity;
import com.playlet.internal.entity.drama.UserWatchHistoryEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * C端剧场：网关 /china/admin/api/theater/**
 */
@RequestMapping("/api/theater")
@Api(value = "剧场", tags = "剧场")
public interface TheaterApiService {

	@GetMapping("/home")
	@ApiOperation(value = "剧场首页")
	ResponseBase home();

	@GetMapping("/rankList")
	@ApiOperation(value = "榜单列表")
	ResponseBase rankList();

	@GetMapping("/rank")
	@ApiImplicitParam(name = "groupId", value = "榜单分组ID", required = true, dataType = "string", paramType = "query")
	@ApiOperation(value = "榜单分页", notes = "读 rank_list，仅 status=1；data=TheaterRankPageRespEntity")
	ResponseBase rank(String groupId, RankListEntity entity);

	@PostMapping("/search")
	@ApiOperation(value = "剧场搜索", notes = "按 dramaTitle 模糊；按 tagId（标签主键，内部转 groupId）或 tagGroupId 精确筛选已上架剧；可单独或组合")
	ResponseBase search(@RequestBody DramaEntity entity, HttpServletRequest request);

}
