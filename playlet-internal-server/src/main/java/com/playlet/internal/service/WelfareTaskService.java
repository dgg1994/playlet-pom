package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.enums.WelfareActionTypeEnums;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * C端福利：网关 /api/welfare/**
 */
@RequestMapping("/api/welfare")
@Api(value = "福利", tags = "福利")
public interface WelfareTaskService {

	@GetMapping("/home")
	@ApiOperation(value = "福利首页", notes = "余额 + 启用任务列表（含进度）；需登录")
	ResponseBase home(HttpServletRequest request);

	@GetMapping("/tasks")
	@ApiOperation(value = "任务列表", notes = "启用任务 + 本周期进度；需登录")
	ResponseBase tasks(HttpServletRequest request);

	@PostMapping("/task/accept")
	@ApiImplicitParam(name = "taskId", value = "任务ID", required = true, dataType = "int", paramType = "query")
	@ApiOperation(value = "领取任务", notes = "领取后本周期才开始累计进度；需登录")
	ResponseBase accept(@RequestParam Integer taskId, HttpServletRequest request);

	@PostMapping("/task/claim")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "taskId", value = "任务ID", required = true, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "adBoost", value = "是否看广告加赠", required = false, dataType = "boolean", paramType = "query")
	})
	@ApiOperation(value = "领取任务奖励", notes = "进度须为可领取奖励；需登录")
	ResponseBase claim(@RequestParam Integer taskId,
			@RequestParam(required = false, defaultValue = "false") Boolean adBoost,
			HttpServletRequest request);

	@GetMapping("/ledger")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "bizType", value = "业务类型筛选", required = false, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "pageNumber", value = "页码", required = false, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "pageSize", value = "每页", required = false, dataType = "int", paramType = "query")
	})
	@ApiOperation(value = "金币流水", notes = "需登录")
	ResponseBase ledger(String bizType, PageQueryHelperEntity page, HttpServletRequest request);

	/**
	 * 行为推进进度（内部调用，非 HTTP）
	 */
	void onAction(String uid, WelfareActionTypeEnums action, int delta, String extInfo);
}
