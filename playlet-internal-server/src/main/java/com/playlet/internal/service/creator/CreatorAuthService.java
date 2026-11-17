package com.playlet.internal.service.creator;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.creator.CreatorMobileLoginQuery;
import com.playlet.internal.query.creator.CreatorMobileSignUpQuery;
import com.playlet.internal.query.creator.CreatorUpdateInfoQuery;
import com.playlet.internal.query.creator.CreatorUpdatePwdQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 作家端登录注册
 */
@RequestMapping("/api/creator")
@Api(value = "作家端账号", tags = "作家端账号")
public interface CreatorAuthService {

	@PostMapping("/signUpByMobile")
	@ApiOperation(value = "手机号注册", notes = "手机号即登录账号；校验短信验证码后写入账号+入驻资料，data 返回 Bearer token", response = ResponseBase.class)
	ResponseBase signUpByMobile(CreatorMobileSignUpQuery query);

	@PostMapping("/loginByMobile")
	@ApiOperation(value = "手机号登录", notes = "手机号 + 密码，或手机号 + 短信验证码；data 返回 Bearer token", response = ResponseBase.class)
	ResponseBase loginByMobile(CreatorMobileLoginQuery query);

	@PostMapping("/updatePwd")
	@ApiOperation(value = "修改密码", notes = "需登录，校验原密码；成功后需重新登录")
	ResponseBase updatePwd(CreatorUpdatePwdQuery query, HttpServletRequest request);

	@PostMapping("/update")
	@ApiOperation(value = "修改用户信息", notes = "需登录；不可改登录邮箱")
	ResponseBase update(CreatorUpdateInfoQuery query, HttpServletRequest request);

	@GetMapping("/findInfo")
	@ApiOperation(value = "当前登录作家资料", notes = "当前登录作家资料")
	ResponseBase findInfo(HttpServletRequest request);

	@GetMapping("/signOut")
	@ApiOperation(value = "退出登录", notes = "吊销当前作家会话；未登录也返回成功，便于前端幂等清理")
	ResponseBase signOut(HttpServletRequest request);
}
