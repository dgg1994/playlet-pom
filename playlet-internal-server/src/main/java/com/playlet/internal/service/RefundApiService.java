package com.playlet.internal.service;

import com.playlet.internal.api.request.RefundApplyCreateQuery;
import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * C端退款申请：网关 /entrance/api/refund/**
 */
@RequestMapping("/api/refund")
@Api(value = "退款申请", tags = "退款申请")
public interface RefundApiService {

	@PostMapping("/apply")
	@ApiOperation(value = "提交退款申请", notes = "需登录；仅已支付会员/单集现金订单；同单待处理不可重复申请")
	ResponseBase apply(HttpServletRequest request, RefundApplyCreateQuery query);
}
