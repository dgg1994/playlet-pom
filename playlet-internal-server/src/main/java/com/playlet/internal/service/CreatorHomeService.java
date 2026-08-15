package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 作家端首页：聚合首屏 + 榜单切换。
 * 网关：/china/admin/api/creator/home/** 或 /entrance/api/creator/home/**
 */
@RequestMapping("/creator/home")
@Api(value = "作家端首页", tags = "作家端首页")
public interface CreatorHomeService {

	@GetMapping("")
	@ApiOperation(value = "首页聚合", notes = "概览 + 热点剧 + 热点题材 + 公告摘要 + 默认影响力榜")
	ResponseBase home(HttpServletRequest request);

	@GetMapping("/rank")
	@ApiImplicitParam(name = "type", value = "1影响力 2成长力，默认1", required = false, dataType = "Integer",
			paramType = "query")
	@ApiOperation(value = "榜单切换", notes = "仅返回影响力/成长力榜列表")
	ResponseBase rank(@RequestParam(value = "type", required = false) Integer type, HttpServletRequest request);
}
