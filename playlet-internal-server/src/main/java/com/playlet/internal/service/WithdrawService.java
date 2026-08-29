package com.playlet.internal.service;

import com.playlet.internal.api.request.BankcardActiveRequest;
import com.playlet.internal.api.request.BankcardCanActiveRequest;
import com.playlet.internal.api.request.BankcardRechargeRequest;
import com.playlet.internal.api.request.BankcardSetPinRequest;
import com.playlet.internal.api.request.BankcardUpdateEmailRequest;
import com.playlet.internal.api.request.BankcardUpdateStatusRequest;
import com.playlet.internal.api.request.BankcardUserIdRequest;
import com.playlet.internal.api.request.KycApplyRequest;
import com.playlet.internal.api.request.KycCountryListRequest;
import com.playlet.internal.api.request.UsdtTopinNotifyRequest;
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
 * C 端钱包提现：网关 /entrance/wallet/**
 */
@RequestMapping("/wallet")
@Api(value = "钱包提现", tags = "钱包提现")
public interface WithdrawService {

	@GetMapping("/withdraw/home")
	@ApiOperation(value = "提现首页", notes = "可用金币 + 可提现资产列表；未登录时金币等为 0，仍返回资产配置")
	ResponseBase withdrawHome(HttpServletRequest request);

	@PostMapping("/withdraw")
	@ApiOperation(value = "提现", notes = "需登录且已开通钱包。按 withdraw_config 比例将金币换算为 U，入账 wallet_account.available_balance。")
	ResponseBase withdraw(@RequestBody WithdrawReqEntity query, HttpServletRequest request);

	@GetMapping("/topinUsdtAddress")
	@ApiOperation(value = "获取USDT充值的钱包地址", notes = "获取USDT充值的钱包地址", response = ResponseBase.class)
	ResponseBase topinUsdtAddress(String uid);

	@PostMapping("/topinUsdtNotify")
	@ApiOperation(value = "USDT充值回调", notes = "网关回调，无需登录；验签后增加钱包可用余额")
	ResponseBase topinUsdtNotify(@RequestBody UsdtTopinNotifyRequest query, HttpServletRequest request);

	@GetMapping("/withdraw/records")
	@ApiOperation(value = "提现记录", notes = "分页；地址脱敏；需登录")
	ResponseBase withdrawRecords(PageQueryHelperEntity page, HttpServletRequest request);

	@PostMapping("/user/bindPayPwd")
	@ApiOperation(value = "绑定支付密码", notes = "首次设置，6位数字；需登录。已设置不可重复绑定。")
	ResponseBase bindPayPwd(@RequestBody WalletBindPayPwdRequest query, HttpServletRequest request);

	@PostMapping("/kyc/country/list")
	@ApiOperation(value = "KYC国家列表", notes = "透传三方；name 不填返回全部；需登录")
	ResponseBase kycCountryList(@RequestBody(required = false) KycCountryListRequest query,
			HttpServletRequest request);

	@GetMapping("/kyc/status")
	@ApiOperation(value = "查询KYC状态", notes = "拉三方并回写本地；需登录")
	ResponseBase kycStatus(HttpServletRequest request);

	@PostMapping("/kyc/apply")
	@ApiOperation(value = "提交KYC信息", notes = "需登录；证件照已上传拿到 url 后提交；审核中/已通过不可重复提交")
	ResponseBase kycApply(@RequestBody KycApplyRequest query, HttpServletRequest request);

	@GetMapping("/card/list")
	@ApiOperation(value = "卡片列表", notes = "默认卡优先；首页切换与卡片列表页复用；需登录")
	ResponseBase cardList(HttpServletRequest request);

	@GetMapping("/card/product/list")
	@ApiOperation(value = "可用卡产品列表", notes = "商户可申请的卡产品，申请开卡前选品；含卡标签 labelList、卡简介 synopsisData；需登录")
	ResponseBase cardProductList(HttpServletRequest request);

	@PostMapping("/card/apply")
	@ApiOperation(value = "申请卡片", notes = "需登录且 KYC 已通过；productId 来自产品列表；实体卡可传 deliveryAddressId")
	ResponseBase applyCard(@RequestBody WalletApplyCardRequest query, HttpServletRequest request);

	@PostMapping("/card/canActive")
	@ApiOperation(value = "银行卡是否可激活", notes = "实体卡激活前校验；需登录")
	ResponseBase cardCanActive(@RequestBody BankcardCanActiveRequest query, HttpServletRequest request);

	@PostMapping("/card/active")
	@ApiOperation(value = "银行卡激活", notes = "实体卡激活；成功后回写本地卡状态；需登录")
	ResponseBase cardActive(@RequestBody BankcardActiveRequest query, HttpServletRequest request);

	@PostMapping("/card/setPin")
	@ApiOperation(value = "设置Pin", notes = "设置 ATM 支付密码；需登录")
	ResponseBase cardSetPin(@RequestBody BankcardSetPinRequest query, HttpServletRequest request);

	@PostMapping("/card/balance")
	@ApiOperation(value = "查询银行卡余额", notes = "拉三方并同步本地余额缓存；需登录")
	ResponseBase cardBalance(@RequestBody BankcardUserIdRequest query, HttpServletRequest request);

	@PostMapping("/card/recharge")
	@ApiOperation(value = "银行卡充值", notes = "需登录；从 wallet_account.available_balance 扣款后充到 U 卡；requestOrderId 幂等")
	ResponseBase cardRecharge(@RequestBody BankcardRechargeRequest query, HttpServletRequest request);

	@PostMapping("/card/updateStatus")
	@ApiOperation(value = "更新银行卡状态", notes = "冻结/解冻；enable=true 解冻，false 冻结；需登录")
	ResponseBase cardUpdateStatus(@RequestBody BankcardUpdateStatusRequest query, HttpServletRequest request);

	@PostMapping("/card/close")
	@ApiOperation(value = "注销银行卡", notes = "关卡；需登录")
	ResponseBase cardClose(@RequestBody BankcardUserIdRequest query, HttpServletRequest request);

	@PostMapping("/card/info")
	@ApiOperation(value = "查询银行卡信息", notes = "含 cvv/明文卡号等敏感信息；需登录")
	ResponseBase cardInfo(@RequestBody BankcardUserIdRequest query, HttpServletRequest request);

	@PostMapping("/card/updateEmail")
	@ApiOperation(value = "更新银行卡邮箱", notes = "需登录")
	ResponseBase cardUpdateEmail(@RequestBody BankcardUpdateEmailRequest query, HttpServletRequest request);

	@PostMapping("/card/queryPin")
	@ApiOperation(value = "查询Pin", notes = "返回 AES 密文；需登录")
	ResponseBase cardQueryPin(@RequestBody BankcardUserIdRequest query, HttpServletRequest request);

	@GetMapping("/transaction/list")
	@ApiOperation(value = "交易记录", notes = "分页；首页可用较小 pageSize，点全部继续翻页；需登录")
	ResponseBase transactionList(PageQueryHelperEntity page, HttpServletRequest request);
}
