package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.creator.CreatorDramaListQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 作家端作品管理（一期：只读列表与详情）。
 * 网关：/china/admin/api/creator/drama/** 或 /entrance/api/creator/drama/**
 */
@RequestMapping("/creator/drama")
@Api(value = "作家端作品", tags = "作家端作品")
public interface CreatorDramaService {

	@PostMapping("/findList")
	@ApiOperation(value = "我的作品列表", notes = "仅剧卡片：封面/标题/总集数/更新时间。剧集走 GET /findInfo?id=")
	ResponseBase findList(CreatorDramaListQuery query, HttpServletRequest request);

	@GetMapping("/findInfo")
	@ApiImplicitParam(name = "id", value = "剧ID", required = true, dataType = "Integer", paramType = "query")
	@ApiOperation(value = "作品详情", notes = "选中某剧后调用；assetList 为剧集表（序列/时长/状态/曝光/完播/上传日期）")
	ResponseBase findInfo(@RequestParam("id") Integer id, HttpServletRequest request);
}
