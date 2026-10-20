package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.member.MemberProductEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端会员套餐：网关 /china/admin/memberProductManage/**
 */
@RequestMapping("/memberProductManage")
@Api(value = "会员套餐管理", tags = "会员套餐管理")
public interface MemberProductManageService {

	@PostMapping("/findList")
	@ApiOperation("套餐分页列表")
	ResponseBase findList(MemberProductEntity entity);

	@PostMapping("/save")
	@ApiOperation("新增套餐")
	ResponseBase save(MemberProductEntity entity);

	@PostMapping("/update")
	@ApiOperation("编辑套餐")
	ResponseBase update(MemberProductEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation("上架/下架")
	ResponseBase changeStatus(MemberProductEntity entity);
}
