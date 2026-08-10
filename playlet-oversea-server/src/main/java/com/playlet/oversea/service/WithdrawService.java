package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import com.playlet.oversea.query.welfare.WithdrawPreviewQuery;
import com.playlet.oversea.query.welfare.WithdrawSubmitQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * C 端钱包提现：网关 /entrance/api/wallet/**
 */
@RequestMapping("/api/wallet")
@Api(value = "钱包提现", tags = "钱包提现")
public interface WithdrawService {

	@GetMapping("/withdraw/home")
	@ApiOperation(value = "提现首页", notes = "可用积分 + 可提现资产列表（币种/网络/汇率/手续费）；需登录")
	ResponseBase withdrawHome(HttpServletRequest request);

	@PostMapping("/withdraw/preview")
	@ApiOperation(value = "提现试算", notes = "传 assetCode+network+points，返回毛额/手续费/实到；需登录")
	ResponseBase withdrawPreview(WithdrawPreviewQuery query, HttpServletRequest request);

	@PostMapping("/withdraw")
	@ApiOperation(value = "提交提现", notes = "传 assetCode+network+地址+积分；扣积分建单并异步打款；需登录")
	ResponseBase withdrawSubmit(WithdrawSubmitQuery query, HttpServletRequest request);

	@GetMapping("/withdraw/records")
	@ApiOperation(value = "提现记录", notes = "分页；地址脱敏；需登录")
	ResponseBase withdrawRecords(PageQueryHelperEntity page, HttpServletRequest request);
}
