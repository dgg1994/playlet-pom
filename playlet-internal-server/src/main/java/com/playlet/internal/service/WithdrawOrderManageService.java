package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.welfare.WithdrawOrderAdminQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端提现订单：网关 /china/admin/withdrawOrderManage/**
 */
@RequestMapping("/withdrawOrderManage")
@Api(value = "提现订单管理", tags = "财务管理-提现记录")
public interface WithdrawOrderManageService {

	@PostMapping("/findUserList")
	@ApiOperation(value = "用户端提现记录", notes = "user_type=1；processFlag：0未处理 1已处理")
	ResponseBase findUserList(WithdrawOrderAdminQuery query);

	@PostMapping("/findCreatorList")
	@ApiOperation(value = "作家端提现记录", notes = "user_type=2；processFlag：0未处理 1已处理")
	ResponseBase findCreatorList(WithdrawOrderAdminQuery query);
}
