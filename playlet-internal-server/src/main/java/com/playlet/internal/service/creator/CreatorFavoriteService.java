package com.playlet.internal.service.creator;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.creator.CreatorFavoriteQuery;
import com.playlet.internal.query.script.ScriptFavoriteQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 我的收藏 - 剧本收藏 / 创作者收藏。
 */
@RequestMapping("/api/creator/favorite")
@Api(value = "作家端-我的收藏", tags = "作家端-我的收藏")
public interface CreatorFavoriteService {

	@GetMapping("/script/list")
	@ApiOperation("剧本收藏列表（分页）")
	ResponseBase scriptList(ScriptFavoriteQuery query, HttpServletRequest request);

	@PostMapping("/script/add")
	@ApiOperation("收藏剧本")
	ResponseBase addScript(@RequestParam("scriptId") Integer scriptId, HttpServletRequest request);

	@PostMapping("/script/cancel")
	@ApiOperation("取消收藏剧本")
	ResponseBase cancelScript(@RequestParam("scriptId") Integer scriptId, HttpServletRequest request);

	@GetMapping("/creator/list")
	@ApiOperation("创作者收藏列表（分页）")
	ResponseBase creatorList(CreatorFavoriteQuery query, HttpServletRequest request);

	@PostMapping("/creator/add")
	@ApiOperation("收藏创作者")
	ResponseBase addCreator(@RequestParam("creatorId") Integer creatorId, HttpServletRequest request);

	@PostMapping("/creator/cancel")
	@ApiOperation("取消收藏创作者")
	ResponseBase cancelCreator(@RequestParam("creatorId") Integer creatorId, HttpServletRequest request);
}
