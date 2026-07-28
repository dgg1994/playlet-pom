package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.version.AppVersionConfigEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端版本配置：网关 /china/admin/versionManage/**
 */
@RequestMapping("/versionManage")
@Api(value = "版本管理", tags = "版本管理")
public interface VersionManageService {

	@PostMapping("/findList")
	@ApiOperation(value = "版本配置分页列表")
	ResponseBase findList(AppVersionConfigEntity entity);

	@PostMapping("/detail")
	@ApiOperation(value = "版本配置详情（含多语言）")
	ResponseBase detail(AppVersionConfigEntity entity);

	@PostMapping("/save")
	@ApiOperation(value = "新增版本配置")
	ResponseBase save(AppVersionConfigEntity entity);

	@PostMapping("/update")
	@ApiOperation(value = "编辑版本配置（i18n 全量覆盖）")
	ResponseBase update(AppVersionConfigEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation(value = "启用/停用")
	ResponseBase changeStatus(AppVersionConfigEntity entity);

	@PostMapping("/delete")
	@ApiOperation(value = "删除版本配置及多语言")
	ResponseBase delete(AppVersionConfigEntity entity);
}
