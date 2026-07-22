package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.welfare.SignInGlobalConfigEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端签到全局配置：网关 /china/admin/signInConfig/**
 */
@RequestMapping("/signInConfig")
@Api(value = "签到全局配置", tags = "签到全局配置")
public interface SignInConfigManageService {

	@PostMapping("/findList")
	@ApiOperation("配置列表")
	ResponseBase findList(SignInGlobalConfigEntity entity);

	@PostMapping("/update")
	@ApiOperation("编辑配置（启用时会停用其它行）")
	ResponseBase update(SignInGlobalConfigEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation("启用/停用（启用时会停用其它行）")
	ResponseBase changeStatus(SignInGlobalConfigEntity entity);

}
