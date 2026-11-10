package com.playlet.internal.service.creator;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.creator.CreatorRepresentativeSaveQuery;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 产品管理（创作者代表作：过往短剧作品展示）。
 */
@RequestMapping("/api/creator/representative")
@Api(value = "作家端-产品管理（代表作）", tags = "作家端-产品管理（代表作）")
public interface CreatorRepresentativeService {

	@PostMapping("/list")
	@ApiOperation("分页列表")
	ResponseBase list(@RequestBody PageQueryHelperEntity query, HttpServletRequest request);

	@PostMapping("/save")
	@ApiOperation("新增或修改代表作")
	ResponseBase save(@RequestBody CreatorRepresentativeSaveQuery query, HttpServletRequest request);

	@PostMapping("/delete")
	@ApiOperation("删除代表作")
	ResponseBase delete(@RequestBody CreatorRepresentativeSaveQuery query, HttpServletRequest request);

	@PostMapping("/toggleShelf")
	@ApiOperation("上架/下架")
	ResponseBase toggleShelf(@RequestParam("id") Integer id, @RequestParam("on") Boolean on, HttpServletRequest request);

	@GetMapping("/categories")
	@ApiOperation("备案分类字典")
	ResponseBase categories(HttpServletRequest request);

	@GetMapping("/detail")
	@ApiOperation("详情")
	ResponseBase detail(@RequestParam("id") Integer id, HttpServletRequest request);
}
