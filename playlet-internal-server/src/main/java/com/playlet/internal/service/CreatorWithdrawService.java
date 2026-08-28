package com.playlet.internal.service;

import com.playlet.internal.api.request.KycApplyRequest;
import com.playlet.internal.api.request.KycCountryListRequest;
import com.playlet.internal.api.request.WalletApplyCardRequest;
import com.playlet.internal.api.request.WalletBindPayPwdRequest;
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

	@GetMapping("/revenue/summary")
	@ApiOperation(value = "收益概览", notes = "今日/累计/待结算收益（金币）、近7日 incomeTrend、OnePay 结算账户；需作家登录")
	ResponseBase revenueSummary(HttpServletRequest request);

	@GetMapping("/withdraw/home")
	@ApiOperation(value = "提现首页", notes = "可用金币 + 可提现资产列表；需作家登录")
	ResponseBase withdrawHome(HttpServletRequest request);

	@PostMapping("/withdraw")
	@ApiOperation(value = "提现", notes = "需登录且已绑定 OnePay。提交后冻结金币，OnePay 确认到账后再扣减。")
	ResponseBase withdraw(@RequestBody WithdrawReqEntity query, HttpServletRequest request);

	@GetMapping("/withdraw/records")
	@ApiOperation(value = "提现记录", notes = "分页；地址脱敏；需作家登录")
	ResponseBase withdrawRecords(PageQueryHelperEntity page, HttpServletRequest request);

	@GetMapping("/fund/records")
	@ApiOperation(value = "资金流水", notes = "分页查询 creator_coin_ledger；按时间倒序；需作家登录")
	ResponseBase fundRecords(PageQueryHelperEntity page, HttpServletRequest request);

	@PostMapping("/user/bindPayPwd")
	@ApiOperation(value = "绑定支付密码", notes = "首次设置，6位数字；需作家登录。已设置不可重复绑定。")
	ResponseBase bindPayPwd(@RequestBody WalletBindPayPwdRequest query, HttpServletRequest request);

	@PostMapping("/kyc/country/list")
	@ApiOperation(value = "KYC国家列表", notes = "透传三方；name 不填返回全部；需作家登录")
	ResponseBase kycCountryList(@RequestBody(required = false) KycCountryListRequest query,
			HttpServletRequest request);

	@GetMapping("/kyc/status")
	@ApiOperation(value = "查询KYC状态", notes = "拉三方并回写本地；需作家登录")
	ResponseBase kycStatus(HttpServletRequest request);

	@PostMapping("/kyc/apply")
	@ApiOperation(value = "提交KYC信息", notes = "需作家登录；审核中/已通过不可重复提交")
	ResponseBase kycApply(@RequestBody KycApplyRequest query, HttpServletRequest request);

	@GetMapping("/card/list")
	@ApiOperation(value = "卡片列表", notes = "默认卡优先；首页切换与卡片列表页复用；需作家登录")
	ResponseBase cardList(HttpServletRequest request);

	@GetMapping("/card/product/list")
	@ApiOperation(value = "可用卡产品列表", notes = "商户可申请的卡产品，申请开卡前选品；需作家登录")
	ResponseBase cardProductList(HttpServletRequest request);

	@PostMapping("/card/apply")
	@ApiOperation(value = "申请卡片", notes = "需登录且 KYC 已通过；productId 来自产品列表；实体卡可传 deliveryAddressId")
	ResponseBase applyCard(@RequestBody WalletApplyCardRequest query, HttpServletRequest request);

	@GetMapping("/transaction/list")
	@ApiOperation(value = "交易记录", notes = "分页；首页可用较小 pageSize，点全部继续翻页；需作家登录")
	ResponseBase transactionList(PageQueryHelperEntity page, HttpServletRequest request);
}
