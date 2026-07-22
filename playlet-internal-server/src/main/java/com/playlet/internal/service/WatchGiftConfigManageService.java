package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.welfare.WatchGiftGlobalConfigEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端观影礼全局配置：网关 /china/admin/watchGiftConfig/**
 */
@RequestMapping("/watchGiftConfig")
@Api(value = "观影礼全局配置", tags = "观影礼全局配置")
public interface WatchGiftConfigManageService {

	@PostMapping("/findList")
	@ApiOperation("配置列表")
	ResponseBase findList(WatchGiftGlobalConfigEntity entity);

	@PostMapping("/update")
	@ApiOperation("编辑配置（启用时会停用其它行）")
	ResponseBase update(WatchGiftGlobalConfigEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation("启用/停用（启用时会停用其它行）")
	ResponseBase changeStatus(WatchGiftGlobalConfigEntity entity);
}
