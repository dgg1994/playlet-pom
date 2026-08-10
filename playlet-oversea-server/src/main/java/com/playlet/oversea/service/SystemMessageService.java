package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * C 端系统消息：网关 /entrance/api/systemMessage/**
 */
@RequestMapping("/api/systemMessage")
@Api(value = "系统消息", tags = "系统消息")
public interface SystemMessageService {

	@GetMapping("/list")
	@ApiOperation(value = "系统消息列表", notes = "广播读扩散 + 个人收件箱合并；需登录")
	ResponseBase list(PageQueryHelperEntity page, HttpServletRequest request);

	@PostMapping("/read")
	@ApiImplicitParam(name = "id", value = "收件箱消息ID（仅个人消息）", required = true, dataType = "long", paramType = "query")
	@ApiOperation(value = "个人消息单条已读", notes = "广播已读请用 read/all 或进列表后的广播游标")
	ResponseBase read(Long id, HttpServletRequest request);

	@PostMapping("/read/all")
	@ApiOperation(value = "全部已读", notes = "收件箱全读 + 广播游标推进到当前最大 publishId")
	ResponseBase readAll(HttpServletRequest request);

	@GetMapping("/unread/count")
	@ApiOperation(value = "未读数", notes = "个人未读 + 广播未读")
	ResponseBase unreadCount(HttpServletRequest request);
}
