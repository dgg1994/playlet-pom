package com.playlet.internal.service;


import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.query.account.BindPushQuery;
import com.playlet.internal.query.account.PushSwitchQuery;
import com.playlet.internal.query.account.UpdatePwdEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/api/appUser")
@Api(value = "app用户", tags = "app用户")
public interface AppUserService {

    @PostMapping("/signUp")
    @ApiOperation(value = "注册", notes = "注册", response = ResponseBase.class)
    ResponseBase signUp(AppAccountEntity entity);

    @PostMapping("/login")
    @ApiOperation(value = "登录", notes = "登录", response = ResponseBase.class)
    ResponseBase login(AppAccountEntity entity, HttpServletRequest req);

    @PostMapping("/oneClickLogin")
    @ApiOperation(value = "一键注册/登录(苹果/谷歌)", notes = "type:1=Apple,2=Google；body 需 idToken。Apple 非首次可传 userEmail。无用户则自动注册+绑定，有则直接登录。返回同 /login 的 token。", response = ResponseBase.class)
    ResponseBase oneClickLogin(AppAccountEntity entity, HttpServletRequest req);
	    
	@GetMapping("/findToken")
	@ApiOperation(value = "根据token获取用户信息", notes = "根据token获取用户信息", response = ResponseBase.class)
	ResponseBase findToken(HttpServletRequest request);

    @GetMapping("/sendEmailCode")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userEmail", value = "邮箱地址", required = true, dataType = "String", paramType = "query"),
    })
    @ApiOperation(value = "发送邮件验证码", notes = "发送邮件验证码", response = ResponseBase.class)
    ResponseBase sendEmailCode(@RequestParam String userEmail);

    @GetMapping("/checkEmailCode")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userEmail", value = "邮箱地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "emailCode", value = "验证码", required = true, dataType = "String", paramType = "query"),
    })
    @ApiOperation(value = "校验邮件验证码", notes = "校验邮件验证码", response = ResponseBase.class)
    ResponseBase checkEmailCode(@RequestParam String userEmail, @RequestParam String emailCode);
    
    @PostMapping("/updatePwd")
	@ApiOperation(value = "修改密码",notes="修改密码",response=ResponseBase.class)
    ResponseBase updatePwd(UpdatePwdEntity entity, HttpServletRequest request);

    @PostMapping("/update")
    @ApiOperation(value = "修改用户信息",notes="修改用户信息",response=ResponseBase.class)
    ResponseBase update(AppAccountEntity entity, HttpServletRequest request);

	
    @PostMapping("/bindPush")
    @ApiOperation(value = "绑定极光推送", notes = "无需登录。App 启动后上报 cid 或 registrationId；可选 deviceName。"
			+ "已登录时会同时写入账号 registration_id，便于互动/勋章推送。", response = ResponseBase.class)
    ResponseBase bindPush(BindPushQuery entity, HttpServletRequest request);

	@GetMapping("/getPushSwitch")
	@ApiOperation(value = "查询极光推送开关", notes = "需登录。返回 enabled：1开启 0关闭，默认开启。", response = ResponseBase.class)
	ResponseBase getPushSwitch(HttpServletRequest request);

	@PostMapping("/setPushSwitch")
	@ApiOperation(value = "设置极光推送开关", notes = "需登录。body: {\"enabled\":1|0}。关闭后不再推送互动/勋章/系统消息等极光通知。", response = ResponseBase.class)
	ResponseBase setPushSwitch(PushSwitchQuery entity, HttpServletRequest request);

	@GetMapping("/signOut")
	@ApiOperation(value = "退出登录",notes="退出登录",response=ResponseBase.class)
    ResponseBase signOut(HttpServletRequest request);
	
	@PostMapping("/forgetPassword")
	@ApiOperation(value = "忘记密码",notes="忘记密码",response=ResponseBase.class)
    ResponseBase forgetPasswrod(UpdatePwdEntity entity);


    @GetMapping("/logout")
    @ApiOperation(value = "注销账户",notes="注销账户",response=ResponseBase.class)
    ResponseBase logout(Integer uid,HttpServletRequest request);

}
