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
 * 作家端消息中心。
 */
@RequestMapping("/creator/message")
@Api(value = "作家端消息", tags = "作家端消息")
public interface CreatorMessageService {

	@PostMapping("/findList")
	@ApiOperation(value = "消息列表", notes = "评审收件箱 + 站务广播合并；需作家登录")
	ResponseBase findList(PageQueryHelperEntity page, HttpServletRequest request);

	@GetMapping("/read")
	@ApiImplicitParam(name = "id", value = "收件箱消息ID（仅 INBOX）", required = true, dataType = "long", paramType = "query")
	@ApiOperation(value = "单条已读", notes = "仅个人收件箱；站务已读请用 readAll")
	ResponseBase read(Long id, HttpServletRequest request);

	@GetMapping("/readAll")
	@ApiOperation(value = "全部已读", notes = "收件箱全读 + 站务游标推进")
	ResponseBase readAll(HttpServletRequest request);

	@GetMapping("/unread/count")
	@ApiOperation(value = "未读数", notes = "个人未读 + 站务广播未读")
	ResponseBase unreadCount(HttpServletRequest request);
}
