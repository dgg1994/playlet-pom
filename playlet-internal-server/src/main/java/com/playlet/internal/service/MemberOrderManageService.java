package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.member.MemberOrderEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端会员订单：网关 /china/admin/memberOrderManage/**
 */
@RequestMapping("/memberOrderManage")
@Api(value = "会员订单管理", tags = "会员订单管理")
public interface MemberOrderManageService {

	@PostMapping("/findList")
	@ApiOperation("订单分页列表")
	ResponseBase findList(MemberOrderEntity entity);
}
