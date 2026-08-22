package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 作家端首页
 */
@RequestMapping("/creator/home")
@Api(value = "作家端首页", tags = "作家端首页")
public interface CreatorHomeService {

	@GetMapping("/stats")
	@ApiOperation(value = "顶部数据概览", notes = "今日/昨日收益、余额、累计（金币）、播放、在播")
	ResponseBase stats(HttpServletRequest request);

	@GetMapping("/feed")
	@ApiOperation(value = "首页Feed", notes = "近期热点剧集 + 热点题材")
	ResponseBase feed(HttpServletRequest request);

	@GetMapping("/notices")
	@ApiOperation(value = "系统公告摘要", notes = "首页公告列表摘要；查看更多可后续扩展分页")
	ResponseBase notices(HttpServletRequest request);

	@GetMapping("/rank")
	@ApiImplicitParam(name = "type", value = "1影响力 2成长力，默认1", required = false, dataType = "Integer",
			paramType = "query")
	@ApiOperation(value = "榜单切换", notes = "仅返回影响力/成长力榜列表")
	ResponseBase rank(@RequestParam(value = "type", required = false) Integer type, HttpServletRequest request);
}
