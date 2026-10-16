package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.RankBoardEntity;
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
	@ApiImplicitParams({
			@ApiImplicitParam(name = "tab", value = "hot热榜 / playlist剧单 / recommend推荐；空默认推荐", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "pageNumber", value = "推荐分页页码，默认1", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "pageSize", value = "推荐分页大小", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "seed", value = "推荐会话种子；首页可空，翻页原样回传", dataType = "string", paramType = "query")
	})
	@ApiOperation(value = "剧场首页", notes = "同一接口按 tab 返回热榜/剧单/推荐；推荐登录且有偏好映射时加权，否则保持 seed 打散")
	ResponseBase home(String tab, Integer pageNumber, Integer pageSize, String seed, HttpServletRequest request);

	@PostMapping("/findList")
	@ApiOperation("榜单定义分页")
	ResponseBase boardFindList(RankBoardEntity entity);

	@GetMapping("/rankList")
	@ApiOperation(value = "榜单列表")
	ResponseBase rankList();

	@GetMapping("/rank")
	@ApiImplicitParam(name = "groupId", value = "榜单分组ID", required = true, dataType = "string", paramType = "query")
	@ApiOperation(value = "榜单分页", notes = "读 rank_list，仅 status=1；data=TheaterRankPageRespEntity")
	ResponseBase rank(String groupId, RankListEntity entity);

	@GetMapping("/playlist")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "groupId", value = "剧单 groupId（首页 blocks.groupId）", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "pageNumber", value = "页码，默认1", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "pageSize", value = "页大小", dataType = "int", paramType = "query")
	})
	@ApiOperation(value = "剧单分页", notes = "点进首页剧单块后拉完整列表；按 match_mode + 绑定标签捞在架剧；data=TheaterPlaylistPageRespEntity")
	ResponseBase playlist(String groupId, Integer pageNumber, Integer pageSize);

	@PostMapping("/search")
	@ApiOperation(value = "剧场搜索", notes = "按 dramaTitle 模糊；按 tagId（标签主键，内部转 groupId）或 tagGroupId 精确筛选已上架剧；可单独或组合")
	ResponseBase search(@RequestBody DramaEntity entity, HttpServletRequest request);

	@PostMapping("/view/report")
	@ApiOperation(value = "上报浏览/观看进度", notes = "需登录。body: dramaId 必填；episodeId/watchProgress 续播；"
			+ "deltaSeconds=本次有效观看秒数（关闭播放器/心跳）；episodeProgress=单集总时长秒（可选，用于热度换算）。"
			+ "副作用：观影礼累计、按集福利(WATCH)、榜单日聚合、热度=有效秒数/(单集时长/3)。")
	ResponseBase reportWatch(@RequestBody UserWatchHistoryEntity entity, HttpServletRequest request);

}
