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
 * 管理端作家首页同款 Feed / 榜单：网关 /china/admin/creatorHomeManage/**
 */
@RequestMapping("/sysHomeManage")
@Api(value = "管理端作家首页数据", tags = "管理端作家首页数据")
public interface SysHomeManageService {

	@GetMapping("/feed")
	@ApiOperation(value = "首页Feed", notes = "与作家端同款：近期热点剧集 + 热点题材；管理端 token")
	ResponseBase feed(HttpServletRequest request);

	@GetMapping("/rank")
	@ApiImplicitParam(name = "type", value = "1影响力 2成长力，默认1", required = false, dataType = "Integer",
			paramType = "query")
	@ApiOperation(value = "榜单切换", notes = "与作家端同款：影响力/成长力榜；管理端 token")
	ResponseBase rank(@RequestParam(value = "type", required = false) Integer type, HttpServletRequest request);
}
