package com.playlet.internal.service;

import com.playlet.internal.api.request.RefundApplyCreateQuery;
import com.playlet.internal.api.request.RefundApplyHandleQuery;
import com.playlet.internal.api.request.RefundApplyQuery;
import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理端退款管理：网关 /china/admin/refundManage/**
 */
@RequestMapping("/refundManage")
@Api(value = "退款管理", tags = "退款管理")
public interface RefundManageService {

	@PostMapping("/findList")
	@ApiOperation("退款申请分页列表")
	ResponseBase findList(RefundApplyQuery query);

	@PostMapping("/agree")
	@ApiOperation("同意退款：标记原单已退款（暂不调渠道）")
	ResponseBase agree(HttpServletRequest request, RefundApplyHandleQuery query);

	@PostMapping("/reject")
	@ApiOperation("拒绝退款申请")
	ResponseBase reject(HttpServletRequest request, RefundApplyHandleQuery query);

	@PostMapping("/create")
	@ApiOperation("管理端代提退款申请（联调/客服代提）")
	ResponseBase create(HttpServletRequest request, RefundApplyCreateQuery query);
}
