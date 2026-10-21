package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.security.SensitiveWordEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理端敏感词库：网关 /china/admin/sensitiveWordManage/**
 */
@RequestMapping("/sensitiveWordManage")
@Api(value = "敏感词管理", tags = "敏感词管理")
public interface SensitiveWordManageService {

	@PostMapping("/findList")
	@ApiOperation("敏感词分页列表（支持 category/status 筛选）")
	ResponseBase findList(SensitiveWordEntity entity);

	@PostMapping("/detail")
	@ApiOperation("敏感词详情")
	ResponseBase detail(SensitiveWordEntity entity);

	@PostMapping("/save")
	@ApiOperation("新增敏感词（默认待审核，需通过后进词库）")
	ResponseBase save(HttpServletRequest request, SensitiveWordEntity entity);

	@PostMapping("/update")
	@ApiOperation("编辑敏感词；已生效词改文案后回待审核")
	ResponseBase update(HttpServletRequest request, SensitiveWordEntity entity);

	@PostMapping("/approve")
	@ApiOperation("审核通过 → 已生效并刷新词库")
	ResponseBase approve(HttpServletRequest request, SensitiveWordEntity entity);

	@PostMapping("/reject")
	@ApiOperation("审核驳回")
	ResponseBase reject(HttpServletRequest request, SensitiveWordEntity entity);

	@PostMapping("/disable")
	@ApiOperation("禁用已生效词并刷新词库")
	ResponseBase disable(HttpServletRequest request, SensitiveWordEntity entity);

	@GetMapping("/delete")
	@ApiOperation("删除敏感词")
	ResponseBase delete(Integer id);

	@PostMapping("/reload")
	@ApiOperation("立即刷新内存词库")
	ResponseBase reload();
}
