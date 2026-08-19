package com.playlet.internal.service;

import com.playlet.internal.api.request.WithdrawReqEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 作家端钱包提现：网关 /china/admin/api/creator/wallet/** 或 /entrance/api/creator/wallet/**
 */
@RequestMapping("/creator/wallet")
@Api(value = "作家端钱包提现", tags = "作家端钱包提现")
public interface CreatorWithdrawService {

	@GetMapping("/withdraw/home")
	@ApiOperation(value = "提现首页", notes = "可用金币 + 可提现资产列表；需作家登录")
	ResponseBase withdrawHome(HttpServletRequest request);

	@PostMapping("/withdraw")
	@ApiOperation(value = "提现", notes = "需登录且已绑定 OnePay。提交后冻结金币，OnePay 确认到账后再扣减。")
	ResponseBase withdraw(@RequestBody WithdrawReqEntity query, HttpServletRequest request);

	@GetMapping("/withdraw/records")
	@ApiOperation(value = "提现记录", notes = "分页；地址脱敏；需作家登录")
	ResponseBase withdrawRecords(PageQueryHelperEntity page, HttpServletRequest request);
}
