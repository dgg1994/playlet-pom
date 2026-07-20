package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.account.UserFollowEntity;
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
 * C端关注/粉丝：网关 /china/admin/api/appUser/**
 */
@RequestMapping("/api/appUser")
@Api(value = "用户关注", tags = "用户关注")
public interface UserFollowService {

	@PostMapping("/follow/add")
	@ApiImplicitParam(name = "followUid", value = "被关注用户uid", required = true, dataType = "string", paramType = "query")
	@ApiOperation(value = "关注用户", notes = "需登录；幂等；不可关注自己")
	ResponseBase followAdd(@RequestParam Integer followUid, HttpServletRequest request);

	@PostMapping("/follow/cancel")
	@ApiImplicitParam(name = "followUid", value = "取消关注的用户uid", required = true, dataType = "string", paramType = "query")
	@ApiOperation(value = "取消关注", notes = "需登录；幂等")
	ResponseBase followCancel(@RequestParam Integer followUid, HttpServletRequest request);

	@GetMapping("/follow/following")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "查看谁的关注，不传默认当前登录用户", required = false, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "pageNumber", value = "页码", required = false, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "pageSize", value = "每页", required = false, dataType = "int", paramType = "query")
	})
	@ApiOperation(value = "关注列表", notes = "我的关注 / 某用户的关注")
	ResponseBase followingList(UserFollowEntity entity, HttpServletRequest request);

	@GetMapping("/follow/fans")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "查看谁的粉丝，不传默认当前登录用户", required = false, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "pageNumber", value = "页码", required = false, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "pageSize", value = "每页", required = false, dataType = "int", paramType = "query")
	})
	@ApiOperation(value = "粉丝列表", notes = "我的粉丝 / 某用户的粉丝")
	ResponseBase fansList(UserFollowEntity entity, HttpServletRequest request);

}
