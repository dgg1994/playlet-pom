package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.ad.AdSlotConfigEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端广告位配置：网关 /china/admin/adSlotConfig/**
 */
@RequestMapping("/adSlotConfig")
@Api(value = "广告位配置", tags = "广告位配置")
public interface AdSlotConfigManageService {

	@PostMapping("/findList")
	@ApiOperation("广告位列表")
	ResponseBase findList(AdSlotConfigEntity entity);

	@PostMapping("/save")
	@ApiOperation("新增广告位")
	ResponseBase save(AdSlotConfigEntity entity);

	@PostMapping("/update")
	@ApiOperation("编辑广告位")
	ResponseBase update(AdSlotConfigEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation("启用/停用")
	ResponseBase changeStatus(AdSlotConfigEntity entity);
}
