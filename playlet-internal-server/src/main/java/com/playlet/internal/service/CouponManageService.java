package com.playlet.internal.service;

import com.playlet.internal.api.request.CouponIssueQuery;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.coupon.CouponIssueLogEntity;
import com.playlet.internal.entity.coupon.CouponTemplateEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理端优惠券：网关 /china/admin/couponManage/**
 */
@RequestMapping("/couponManage")
@Api(value = "优惠券管理", tags = "优惠券管理")
public interface CouponManageService {

	@PostMapping("/findList")
	@ApiOperation("券模板分页列表（含面额文案、已发放）")
	ResponseBase findList(CouponTemplateEntity entity);

	@PostMapping("/detail")
	@ApiOperation("券模板详情")
	ResponseBase detail(CouponTemplateEntity entity);

	@PostMapping("/save")
	@ApiOperation("新增券模板")
	ResponseBase save(CouponTemplateEntity entity);

	@PostMapping("/update")
	@ApiOperation("编辑券模板")
	ResponseBase update(CouponTemplateEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation("启用/停用")
	ResponseBase changeStatus(CouponTemplateEntity entity);

	@PostMapping("/issue")
	@ApiOperation("发放优惠券")
	ResponseBase issue(HttpServletRequest request, CouponIssueQuery query);

	@PostMapping("/issueLogList")
	@ApiOperation("发放流水分页")
	ResponseBase issueLogList(CouponIssueLogEntity entity);
}
