package com.playlet.internal.service;

import com.playlet.internal.api.request.DanmuDeleteRequest;
import com.playlet.internal.api.request.DanmuListRequest;
import com.playlet.internal.api.request.DanmuSendRequest;
import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * C端弹幕：网关 /entrance/api/danmu/**
 */
@RequestMapping("/api/danmu")
@Api(value = "弹幕", tags = "弹幕")
public interface DramaDanmuApiService {

	@PostMapping("/send")
	@ApiOperation(value = "发送弹幕", notes = "需登录；body: videoId/content/playTime；可选 color/mode/fontSize")
	ResponseBase send(@RequestBody DanmuSendRequest request, HttpServletRequest httpRequest);

	@PostMapping("/list")
	@ApiOperation(value = "弹幕列表", notes = "按 videoId + fromTime/toTime 时间窗拉取；可未登录")
	ResponseBase list(@RequestBody DanmuListRequest request);

	@PostMapping("/delete")
	@ApiOperation(value = "删除弹幕", notes = "需登录；仅能删自己的弹幕")
	ResponseBase delete(@RequestBody DanmuDeleteRequest request, HttpServletRequest httpRequest);
}
