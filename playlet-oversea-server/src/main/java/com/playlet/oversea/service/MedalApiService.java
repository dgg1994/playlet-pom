package com.playlet.oversea.service;

import com.playlet.oversea.api.request.MedalAckNotifyRequest;
import com.playlet.oversea.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * C端勋章：网关 /entrance/api/medal/**
 */
@RequestMapping("/api/medal")
@Api(value = "C端勋章管理", tags = "C端勋章管理")
public interface MedalApiService {

	@GetMapping("/findList")
	@ApiOperation(value = "勋章列表", notes = "未登录返回未解锁图标；已登录按解锁状态返回对应图标")
	ResponseBase findMedalList(HttpServletRequest request);

	@GetMapping("/detail")
	@ApiImplicitParam(name = "id", value = "勋章ID", required = true, dataType = "int", paramType = "query")
	@ApiOperation(value = "勋章详情", notes = "返回名称/Slogan/条件文案/图标/进度/解锁时间等；登录后带用户进度。"
			+ "请求示例：GET /api/medal/detail?id=1")
	ResponseBase findMedalDetail(Integer id, HttpServletRequest request);

	@PostMapping("/ackNotify")
	@ApiOperation(value = "确认已弹窗", notes = "需登录；将已解锁且未提醒的勋章标记为已提醒（notify_status=1），支持单条/批量。"
			+ "请求示例：{\"medalIds\":[1,2]}")
	ResponseBase ackNotify(MedalAckNotifyRequest request, HttpServletRequest httpRequest);

	@GetMapping("/pendingNotify")
	@ApiOperation(value = "待弹窗解锁列表", notes = "需登录；返回 unlocked=1 且 notify_status=0 的勋章")
	ResponseBase pendingNotify(HttpServletRequest request);
}
