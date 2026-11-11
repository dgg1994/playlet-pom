package com.playlet.internal.service.creator;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.creator.CreatorMessageQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 作家端消息中心。
 */
@RequestMapping("/creator/message")
@Api(value = "作家端消息", tags = "作家端消息")
public interface CreatorMessageService {

	@PostMapping("/findList")
	@ApiOperation(value = "消息列表", notes = "评审收件箱 + 站务广播合并；支持分类 category 与时间区间 startTime/endTime；需作家登录")
	ResponseBase findList(@RequestBody CreatorMessageQuery page, HttpServletRequest request);

	@GetMapping("/read")
	@ApiImplicitParam(name = "id", value = "收件箱消息ID（仅 INBOX）", required = true, dataType = "long", paramType = "query")
	@ApiOperation(value = "单条已读", notes = "收件箱按 id 已读；站务广播按 publishId 推进已读游标")
	ResponseBase read(Long id, Long publishId, HttpServletRequest request);

	@GetMapping("/readAll")
	@ApiOperation(value = "全部已读", notes = "收件箱全读 + 站务游标推进")
	ResponseBase readAll(HttpServletRequest request);

	@GetMapping("/unread/count")
	@ApiOperation(value = "未读数", notes = "个人未读 + 站务广播未读")
	ResponseBase unreadCount(HttpServletRequest request);
}
