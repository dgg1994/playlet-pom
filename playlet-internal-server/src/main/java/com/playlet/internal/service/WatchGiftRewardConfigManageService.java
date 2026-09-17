package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.welfare.WatchGiftRewardConfigEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端观影礼奖励阶梯配置：网关 /china/admin/watchGiftRewardConfig/**
 */
@RequestMapping("/watchGiftRewardConfig")
@Api(value = "观影礼奖励阶梯", tags = "观影礼奖励阶梯")
public interface WatchGiftRewardConfigManageService {

	@PostMapping("/findList")
	@ApiOperation("奖励阶梯分页列表")
	ResponseBase findList(WatchGiftRewardConfigEntity entity);

	@PostMapping("/save")
	@ApiOperation("新增奖励阶梯")
	ResponseBase save(WatchGiftRewardConfigEntity entity);

	@PostMapping("/update")
	@ApiOperation("编辑奖励阶梯")
	ResponseBase update(WatchGiftRewardConfigEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation("启用/停用")
	ResponseBase changeStatus(WatchGiftRewardConfigEntity entity);

	@PostMapping("/delete")
	@ApiOperation("删除奖励阶梯")
	ResponseBase delete(WatchGiftRewardConfigEntity entity);
}
