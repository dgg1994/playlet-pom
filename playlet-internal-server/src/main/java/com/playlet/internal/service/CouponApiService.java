package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * C 端优惠券：网关 /entrance/api/coupon/**
 */
@RequestMapping("/api/coupon")
@Api(value = "优惠券", tags = "优惠券")
public interface CouponApiService {

	@GetMapping("/myList")
	@ApiOperation(value = "我的优惠券", notes = "需登录；status 可选 0未用 1已用 2过期 3作废，不传返回全部")
	ResponseBase myList(HttpServletRequest request,
			@RequestParam(value = "status", required = false) Integer status);
}
