package com.playlet.internal.service;

import com.playlet.internal.api.request.*;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.wallet.WalletLogEntity;
import com.playlet.internal.entity.wallet.WalletTransfetListEntity;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * 作家端钱包提现：网关 /china/admin/api/creator/wallet/** 或 /entrance/api/creator/wallet/**
 */
@RequestMapping("/creator/wallet")
@Api(value = "作家端钱包提现", tags = "作家端钱包提现")
public interface CreatorWithdrawService {

	@GetMapping("/revenue/summary")
	@ApiOperation(value = "收益概览", notes = "今日/累计/待结算收益（金币）、近7日 incomeTrend、U 卡结算账户；需作家登录")
	ResponseBase revenueSummary(HttpServletRequest request);

	@GetMapping("/withdraw/home")
	@ApiOperation(value = "提现首页", notes = "可用金币 + 可提现资产列表；需作家登录")
	ResponseBase withdrawHome(HttpServletRequest request);

	@PostMapping("/withdraw")
	@ApiOperation(value = "提现", notes = "需作家登录且已开通钱包。按 withdraw_config 比例将金币换算为 U，入账 wallet_account.available_balance。")
	ResponseBase withdraw(@RequestBody WithdrawReqEntity query, HttpServletRequest request);

	@GetMapping("/withdraw/records")
	@ApiOperation(value = "提现记录", notes = "分页；地址脱敏；需作家登录")
	ResponseBase withdrawRecords(PageQueryHelperEntity page, HttpServletRequest request);

	@GetMapping("/topinUsdtAddress")
	@ApiOperation(value = "获取USDT充值的钱包地址", notes = "获取USDT充值的钱包地址；query 参数 uid 为钱包三方 uid", response = ResponseBase.class)
	ResponseBase topinUsdtAddress(String uid);


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

	@GetMapping("/kyc/applyByCardApply")
	@ApiOperation(value = "按开卡申请提交KYC", notes = "根据申请单持卡人/KYC快照提交三方；需作家登录")
	ResponseBase kycApplyByCardApply(Long applyId, HttpServletRequest request);

	@PostMapping("/file/upload")
	@ApiOperation(value = "KYC证件上传", notes = "multipart 字段 idCard；透传三方 /api/file/upload；返回 fileUrl；需作家登录")
	ResponseBase kycFileUpload(@RequestParam("idCard") MultipartFile idCard,
			@RequestParam(value = "certType", required = false) Integer certType,
			@RequestParam(value = "documentType", required = false) Integer documentType,
			HttpServletRequest request);

	@GetMapping("/card/list")
	@ApiOperation(value = "卡片列表", notes = "默认卡优先；首页切换与卡片列表页复用；需作家登录")
	ResponseBase cardList(HttpServletRequest request);

	@GetMapping("/card/findUserCardInfo")
	@ApiOperation(value = "持有银行卡详情", notes = "对齐 onetoken /appUserCard/findUserCardInfo；"
			+ "id 为本地 wallet_bankcard.id；含持卡人、卡产品、实体卡物流轨迹、"
			+ "月服务费/USD充值/USDT充值/提现手续费；需作家登录")
	ResponseBase findUserCardInfo(Long id, HttpServletRequest request);

	@PostMapping("/card/findList")
	@ApiOperation(value = "银行卡信息列表", notes = "对齐 onetoken /card/findList；可按 bankCardNature 筛选虚拟/实体卡；需作家登录")
	ResponseBase cardFindList(@RequestBody(required = false) WalletCardProductListRequest query,
			HttpServletRequest request);

	@GetMapping("/card/product/findById")
	@ApiOperation(value = "卡产品详情", notes = "按 productId 查询可申请卡产品；含 labelList、synopsisData；需作家登录")
	ResponseBase cardProductDetail(Integer productId, HttpServletRequest request);

	@GetMapping("/card/findLogistics")
	@ApiOperation(value = "查询物流跟踪", notes = "实体卡物流轨迹；需作家登录")
	ResponseBase findLogistics(String logisticsNum, Long applyId, HttpServletRequest request);

	@PostMapping("/card/apply")
	@ApiOperation(value = "申请开卡", notes = "需作家登录；实体卡须 mailingAddress 或 deliveryAddressId；"
			+ "邮费 logisticsMonery 不传取产品默认；冻结总费用含邮费；KYC 未通过可后补")
	ResponseBase applyCard(@RequestBody WalletApplyCardRequest query, HttpServletRequest request);

	@PostMapping("/cardholder/add")
	@ApiOperation(value = "新增持卡人", notes = "需作家登录且已开通钱包；出生日期须满18岁")
	ResponseBase cardholderAdd(@RequestBody WalletCardholderSaveRequest query, HttpServletRequest request);

	@PostMapping("/cardholder/update")
	@ApiOperation(value = "编辑持卡人", notes = "需作家登录；id 必传且须为本人持卡人")
	ResponseBase cardholderUpdate(@RequestBody WalletCardholderSaveRequest query, HttpServletRequest request);

	@GetMapping("/cardholder/delete")
	@ApiOperation(value = "删除持卡人", notes = "需作家登录；id 为持卡人主键")
	ResponseBase cardholderDelete(Long id, HttpServletRequest request);

	@GetMapping("/cardholder/findByUid")
	@ApiOperation(value = "查询持卡人列表", notes = "返回当前作家的全部持卡人；需作家登录")
	ResponseBase cardholderFindByUid(HttpServletRequest request);

	@GetMapping("/cardholder/findById")
	@ApiOperation(value = "查询持卡人详情", notes = "需作家登录；id 为持卡人主键")
	ResponseBase cardholderFindById(Long id, HttpServletRequest request);

	@PostMapping("/card/canActive")
	@ApiOperation(value = "银行卡是否可激活", notes = "实体卡激活前校验；需作家登录")
	ResponseBase cardCanActive(@RequestBody BankcardCanActiveRequest query, HttpServletRequest request);

	@PostMapping("/card/active")
	@ApiOperation(value = "银行卡激活", notes = "实体卡激活；成功后回写本地卡状态；需作家登录")
	ResponseBase cardActive(@RequestBody BankcardActiveRequest query, HttpServletRequest request);

	@PostMapping("/card/setPin")
	@ApiOperation(value = "设置Pin", notes = "设置 ATM 支付密码；需作家登录")
	ResponseBase cardSetPin(@RequestBody BankcardSetPinRequest query, HttpServletRequest request);

	@PostMapping("/card/balance")
	@ApiOperation(value = "查询银行卡余额", notes = "拉三方并同步本地余额缓存；需作家登录")
	ResponseBase cardBalance(@RequestBody BankcardUserIdRequest query, HttpServletRequest request);

	@PostMapping("/card/recharge")
	@ApiOperation(value = "银行卡充值", notes = "需作家登录；从 wallet_account.available_balance 扣款后充到 U 卡；requestOrderId 幂等")
	ResponseBase cardRecharge(@RequestBody BankcardRechargeRequest query, HttpServletRequest request);

	@PostMapping("/card/updateStatus")
	@ApiOperation(value = "更新银行卡状态", notes = "冻结/解冻；enable=true 解冻，false 冻结；需作家登录")
	ResponseBase cardUpdateStatus(@RequestBody BankcardUpdateStatusRequest query, HttpServletRequest request);

	@PostMapping("/card/close")
	@ApiOperation(value = "注销银行卡", notes = "关卡；需作家登录")
	ResponseBase cardClose(@RequestBody BankcardUserIdRequest query, HttpServletRequest request);

	@PostMapping("/card/info")
	@ApiOperation(value = "查询银行卡信息", notes = "含 cvv/明文卡号等敏感信息；需作家登录")
	ResponseBase cardInfo(@RequestBody BankcardUserIdRequest query, HttpServletRequest request);

	@PostMapping("/card/updateEmail")
	@ApiOperation(value = "更新银行卡邮箱", notes = "需作家登录")
	ResponseBase cardUpdateEmail(@RequestBody BankcardUpdateEmailRequest query, HttpServletRequest request);

	@PostMapping("/card/queryPin")
	@ApiOperation(value = "查询Pin", notes = "返回 AES 密文；需作家登录")
	ResponseBase cardQueryPin(@RequestBody BankcardUserIdRequest query, HttpServletRequest request);

	@GetMapping("/transaction/list")
	@ApiOperation(value = "交易记录", notes = "分页；首页可用较小 pageSize，点全部继续翻页；需作家登录")
	ResponseBase transactionList(PageQueryHelperEntity page, HttpServletRequest request);

	@PostMapping("/transfer")
	@ApiOperation(value = "钱包内部转账", notes = "需作家登录；recipientEmail 为收款人邮箱；sendMoney 为转出金额；payPassword 必填")
	ResponseBase transfer(@RequestBody WalletTransfetListEntity query, HttpServletRequest request);

	@GetMapping("/transferReading")
	@ApiOperation(value = "转账试算", notes = "返回手续费与实际到账；需作家登录")
	ResponseBase transferReading(Double sendMoney, HttpServletRequest request);

	@GetMapping("/findReading")
	@ApiOperation(value = "查询转账费率", notes = "需作家登录")
	ResponseBase findReading(HttpServletRequest request);

	@PostMapping("/walletLog")
	@ApiOperation(value = "钱包账变记录", notes = "分页；默认当月；需作家登录")
	ResponseBase walletLog(@RequestBody(required = false) WalletLogEntity query, HttpServletRequest request);
}
