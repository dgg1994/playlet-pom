package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.drama.TagEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 短剧标签管理：网关路径 /china/admin/api/tag/**
 */
@RequestMapping("/api/tag")
@Api(value = "短剧标签", tags = "短剧标签")
public interface TagService {

	@PostMapping("/findList")
	@ApiOperation("标签分页列表")
	ResponseBase findList(TagEntity entity);

	@GetMapping("/detail")
	@ApiOperation("标签详情")
	ResponseBase detail(String tagId);

	@PostMapping("/save")
	@ApiOperation("新增标签")
	ResponseBase save(TagEntity entity);

	@PostMapping("/update")
	@ApiOperation("编辑标签")
	ResponseBase update(TagEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation("启用/停用标签")
	ResponseBase changeStatus(TagEntity entity);

}
