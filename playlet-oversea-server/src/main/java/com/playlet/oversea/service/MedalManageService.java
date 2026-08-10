package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.medal.MedalConfigEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端勋章配置：网关 /china/admin/medalManage/**
 */
@RequestMapping("/medalManage")
@Api(value = "勋章管理", tags = "勋章管理")
public interface MedalManageService {

	@PostMapping("/findList")
	@ApiOperation("勋章配置分页列表")
	ResponseBase findList(MedalConfigEntity entity);

	@PostMapping("/detail")
	@ApiOperation("勋章配置详情（含多语言）")
	ResponseBase detail(MedalConfigEntity entity);

	@PostMapping("/save")
	@ApiOperation(value = "新增勋章配置")
	ResponseBase save(MedalConfigEntity entity);

	@PostMapping("/update")
	@ApiOperation(value = "编辑勋章配置")
	ResponseBase update(MedalConfigEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation("启用/停用")
	ResponseBase changeStatus(MedalConfigEntity entity);

	@PostMapping("/delete")
	@ApiOperation("软删除勋章配置")
	ResponseBase delete(MedalConfigEntity entity);
}
