package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.welfare.SignInRewardConfigEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端签到奖励阶梯配置：网关 /china/admin/signInRewardConfig/**
 */
@RequestMapping("/signInRewardConfig")
@Api(value = "签到奖励阶梯", tags = "签到奖励阶梯")
public interface SignInRewardConfigManageService {

	@PostMapping("/findList")
	@ApiOperation("奖励阶梯分页列表")
	ResponseBase findList(SignInRewardConfigEntity entity);

	@PostMapping("/save")
	@ApiOperation("新增奖励阶梯")
	ResponseBase save(SignInRewardConfigEntity entity);

	@PostMapping("/update")
	@ApiOperation("编辑奖励阶梯")
	ResponseBase update(SignInRewardConfigEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation("启用/停用")
	ResponseBase changeStatus(SignInRewardConfigEntity entity);

	@PostMapping("/delete")
	@ApiOperation("删除奖励阶梯")
	ResponseBase delete(SignInRewardConfigEntity entity);
}
