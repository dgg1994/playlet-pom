package com.playlet.internal.service.script;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.script.ScriptFavoriteQuery;
import com.playlet.internal.query.script.ScriptMarketListQuery;
import com.playlet.internal.query.script.ScriptTradeApplyQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * C 端剧本市场：浏览、收藏、购买/议价申请。
 */
@RequestMapping("/api/script")
@Api(value = "剧本市场", tags = "剧本市场")
public interface ScriptMarketService {

	@GetMapping("/market/filters")
	@ApiOperation(value = "筛选项字典", notes = "题材/分区/授权/价格/规模/状态 + 5 种排序")
	ResponseBase filters();

	@GetMapping("/market/list")
	@ApiOperation(value = "剧本市场列表", notes = "仅返回审核通过且已上架剧本；支持关键词与 6 组筛选")
	ResponseBase list(ScriptMarketListQuery query, HttpServletRequest request);

	@GetMapping("/market/detail")
	@ApiImplicitParam(name = "id", value = "剧本ID", required = true, dataType = "Integer", paramType = "query")
	@ApiOperation(value = "剧本详情", notes = "带 token 返回 favorited / myApply；不可见返回 404")
	ResponseBase detail(@RequestParam("id") Integer id, HttpServletRequest request);

	@GetMapping("/market/recommendAuthors")
	@ApiOperation(value = "推荐编剧", notes = "limit 默认 4，最大 10；带 token 返回 followed")
	ResponseBase recommendAuthors(@RequestParam(value = "limit", required = false) Integer limit,
			HttpServletRequest request);

	@GetMapping("/market/latest")
	@ApiOperation(value = "最新发布", notes = "审核通过且已上架，按发布时间倒序，最多 10 条")
	ResponseBase latest(@RequestParam(value = "limit", required = false) Integer limit);

	@PostMapping("/favorite/add")
	@ApiOperation(value = "收藏剧本", notes = "幂等；收藏成功计数 +1")
	ResponseBase favoriteAdd(@RequestParam("scriptId") Integer scriptId, HttpServletRequest request);

	@PostMapping("/favorite/cancel")
	@ApiOperation(value = "取消收藏", notes = "幂等；取消成功计数 -1")
	ResponseBase favoriteCancel(@RequestParam("scriptId") Integer scriptId, HttpServletRequest request);

	@GetMapping("/favorite/list")
	@ApiOperation(value = "我的收藏", notes = "分页返回，额外 favoritedAt")
	ResponseBase favoriteList(ScriptFavoriteQuery query, HttpServletRequest request);

	@PostMapping("/trade/apply")
	@ApiOperation(value = "购买/议价申请", notes = "不能申请自己的剧本；仅待售/议价中；同人同剧本仅一条待处理")
	ResponseBase tradeApply(@Valid @RequestBody ScriptTradeApplyQuery query, HttpServletRequest request);
}
