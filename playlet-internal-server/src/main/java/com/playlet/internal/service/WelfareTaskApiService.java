package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * C端福利：网关 /api/welfare/**
 */
@RequestMapping("/api/welfare")
@Api(value = "福利", tags = "福利")
public interface WelfareTaskApiService {

	@GetMapping("/home")
	@ApiOperation(value = "福利首页", notes = "余额 + 签到摘要 + 启用任务列表（含进度）；需登录")
	ResponseBase home(HttpServletRequest request);

	@GetMapping("/tasks")
	@ApiOperation(value = "任务列表", notes = "启用任务 + 本周期进度；需登录")
	ResponseBase tasks(HttpServletRequest request);

}
