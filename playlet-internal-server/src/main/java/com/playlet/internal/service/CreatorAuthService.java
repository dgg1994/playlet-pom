package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.creator.CreatorForgetPwdQuery;
import com.playlet.internal.query.creator.CreatorLoginQuery;
import com.playlet.internal.query.creator.CreatorSignUpQuery;
import com.playlet.internal.query.creator.CreatorUpdateInfoQuery;
import com.playlet.internal.query.creator.CreatorUpdatePwdQuery;
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
 * 作家端登录注册
 */
@RequestMapping("/api/creator")
@Api(value = "作家端账号", tags = "作家端账号")
public interface CreatorAuthService {

	@GetMapping("/sendEmailCode")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "userAccount", value = "登录邮箱", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "scene", value = "场景：1注册 2找回密码，默认1", required = false, dataType = "Integer", paramType = "query")
	})
	@ApiOperation("发送邮箱验证码")
	ResponseBase sendEmailCode(@RequestParam("userAccount") String userAccount,
			@RequestParam(value = "scene", required = false) Integer scene);

	@PostMapping("/signUp")
	@ApiOperation(value = "注册", notes = "userAccount 即邮箱；校验验证码后写入账号+入驻资料。data 返回 Bearer token，同 /api/appUser/signUp", response = ResponseBase.class)
	ResponseBase signUp(CreatorSignUpQuery query);

	@PostMapping("/login")
	@ApiOperation(value = "登录", notes = "邮箱 + 密码。data 返回 Bearer token，同 /api/appUser/login", response = ResponseBase.class)
	ResponseBase login(CreatorLoginQuery query);

	@PostMapping("/forgetPassword")
	@ApiOperation(value = "忘记密码", notes = "邮箱验证码重置密码，并踢掉已登录会话")
	ResponseBase forgetPassword(CreatorForgetPwdQuery query);

	@PostMapping("/updatePwd")
	@ApiOperation(value = "修改密码", notes = "需登录，校验原密码；成功后需重新登录")
	ResponseBase updatePwd(CreatorUpdatePwdQuery query, HttpServletRequest request);

	@PostMapping("/update")
	@ApiOperation(value = "修改用户信息", notes = "需登录；不可改登录邮箱")
	ResponseBase update(CreatorUpdateInfoQuery query, HttpServletRequest request);

	@GetMapping("/findInfo")
	@ApiOperation("当前登录作家资料")
	ResponseBase findInfo(HttpServletRequest request);

	@GetMapping("/signOut")
	@ApiOperation("退出登录")
	ResponseBase signOut(HttpServletRequest request);
}
