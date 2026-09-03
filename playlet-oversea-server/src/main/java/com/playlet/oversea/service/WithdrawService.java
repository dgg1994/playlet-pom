package com.playlet.oversea.service;

import com.playlet.oversea.api.request.*;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.wallet.WalletLogEntity;
import com.playlet.oversea.entity.wallet.WalletTransfetListEntity;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
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

	@GetMapping("/kyc/applyByCardApply")
	@ApiOperation(value = "按开卡申请提交KYC", notes = "对齐 worldpay GET /kyc/apply；根据申请单持卡人/KYC快照组装并提交三方；需登录且申请单归属当前用户")
	ResponseBase kycApplyByCardApply(Long applyId, HttpServletRequest request);

	@PostMapping("/file/upload")
	@ApiOperation(value = "KYC证件上传", notes = "multipart 字段 idCard；透传三方 /api/file/upload；返回 fileUrl；需登录")
	ResponseBase kycFileUpload(@RequestParam("idCard") MultipartFile idCard,
			@RequestParam(value = "certType", required = false) Integer certType,
			@RequestParam(value = "documentType", required = false) Integer documentType,
			HttpServletRequest request);

	@GetMapping("/card/list")
	@ApiOperation(value = "卡片列表", notes = "默认卡优先；首页切换与卡片列表页复用；需登录")
	ResponseBase cardList(HttpServletRequest request);

	@GetMapping("/card/findUserCardInfo")
	@ApiOperation(value = "持有银行卡详情", notes = "对齐 onetoken /appUserCard/findUserCardInfo；"
			+ "id 为本地 wallet_bankcard.id；含持卡人、卡产品、实体卡物流轨迹、"
			+ "月服务费/USD充值/USDT充值/提现手续费；需登录")
	ResponseBase findUserCardInfo(Long id, HttpServletRequest request);

	@PostMapping("/card/upTag")
	@ApiOperation(value = "修改银行卡标签", notes = "对齐 onetoken POST /appUserCard/upTag；"
			+ "传 userBankcardId、tag；需登录且卡须归属当前用户")
	ResponseBase upCardTag(@RequestBody WalletCardTagRequest query, HttpServletRequest request);

	@PostMapping("/card/findList")
	@ApiOperation(value = "银行卡信息列表", notes = "对齐 onetoken /card/findList；可按 bankCardNature=VIRTUAL|PHYSICAL 筛选；"
			+ "返回卡规则：开卡费/预存费/月服务费/卡最大余额；实体卡含 logisticsMonery 邮费；需登录")
	ResponseBase cardFindList(@RequestBody(required = false) WalletCardProductListRequest query,
			HttpServletRequest request);

	@GetMapping("/card/product/findById")
	@ApiOperation(value = "卡产品详情", notes = "按 productId 查询可申请卡产品；含 labelList、synopsisData；需登录")
	ResponseBase cardProductDetail(Integer productId, HttpServletRequest request);

	@GetMapping("/card/findLogistics")
	@ApiOperation(value = "查询物流跟踪", notes = "实体卡物流轨迹；需登录且申请单归属当前用户")
	ResponseBase findLogistics(String logisticsNum, Long applyId, HttpServletRequest request);

	@PostMapping("/card/apply")
	@ApiOperation(value = "申请开卡", notes = "需登录；传 productId、payPassword（6位）；holderId 或 holderData 必传其一；"
			+ "实体卡须传 mailingAddress 或 deliveryAddressId，邮费 logisticsMonery 不传则取卡产品默认值；"
			+ "须已上传 KYC 证件或在 kycData 中带证件照；"
			+ "requestOrderId 可选（不传服务端自动生成 CA 前缀单号），同单号幂等；"
			+ "校验通过后落申请单(待激活)并冻结开卡总费用(月费+开卡费+预存费+邮费)；"
			+ "KYC 未通过时可后补 /wallet/kyc/applyByCardApply 或 /wallet/kyc/apply；"
			+ "KYC 已通过时虚拟卡自动调三方发卡并首充，申请单直接为激活成功(applyState=3)；返回 applyId/费用明细/kycState 等")
	ResponseBase applyCard(@RequestBody WalletApplyCardRequest query, HttpServletRequest request);

	@PostMapping("/cardholder/add")
	@ApiOperation(value = "新增持卡人", notes = "需登录且已开通钱包；出生日期须满18岁")
	ResponseBase cardholderAdd(@RequestBody WalletCardholderSaveRequest query, HttpServletRequest request);

	@PostMapping("/cardholder/update")
	@ApiOperation(value = "编辑持卡人", notes = "需登录；id 必传且须为本人持卡人")
	ResponseBase cardholderUpdate(@RequestBody WalletCardholderSaveRequest query, HttpServletRequest request);

	@GetMapping("/cardholder/delete")
	@ApiOperation(value = "删除持卡人", notes = "需登录；id 为持卡人主键")
	ResponseBase cardholderDelete(Long id, HttpServletRequest request);

	@GetMapping("/cardholder/findByUid")
	@ApiOperation(value = "查询持卡人列表", notes = "返回当前登录用户的全部持卡人；需登录")
	ResponseBase cardholderFindByUid(HttpServletRequest request);

	@GetMapping("/cardholder/findById")
	@ApiOperation(value = "查询持卡人详情", notes = "需登录；id 为持卡人主键")
	ResponseBase cardholderFindById(Long id, HttpServletRequest request);

	@PostMapping("/card/canActive")
	@ApiOperation(value = "银行卡是否可激活", notes = "实体卡激活前校验；需登录")
	ResponseBase cardCanActive(@RequestBody BankcardCanActiveRequest query, HttpServletRequest request);

	@PostMapping("/card/active")
	@ApiOperation(value = "银行卡激活", notes = "实体卡激活；成功后回写本地卡状态；需登录")
	ResponseBase cardActive(@RequestBody BankcardActiveRequest query, HttpServletRequest request);

	@PostMapping("/card/setPin")
	@ApiOperation(value = "设置Pin", notes = "设置 ATM Pin；需登录；传 userBankcardId、pin、payPassword（6位）")
	ResponseBase cardSetPin(@RequestBody BankcardSetPinRequest query, HttpServletRequest request);

	@PostMapping("/card/balance")
	@ApiOperation(value = "查询银行卡余额", notes = "拉三方并同步本地余额缓存；需登录")
	ResponseBase cardBalance(@RequestBody BankcardUserIdRequest query, HttpServletRequest request);

	@PostMapping("/card/recharge")
	@ApiOperation(value = "银行卡充值", notes = "amount 为钱包扣款总额（含手续费），卡到账 = amount - 手续费；"
			+ "handlingFees 不传则按卡产品 rechargeFee 自总额反算；需登录")
	ResponseBase cardTopUp(@RequestBody BankcardRechargeRequest query, HttpServletRequest request);

	@PostMapping("/card/updateStatus")
	@ApiOperation(value = "更新银行卡状态", notes = "冻结/解冻；enable=true 解冻，false 冻结；需登录")
	ResponseBase cardUpdateStatus(@RequestBody BankcardUpdateStatusRequest query, HttpServletRequest request);

	@PostMapping("/card/close")
	@ApiOperation(value = "注销银行卡", notes = "关卡；需登录；传 userBankcardId、payPassword（6位）")
	ResponseBase cardClose(@RequestBody BankcardCloseRequest query, HttpServletRequest request);

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
	@ApiOperation(value = "交易记录", notes = "分页；可选 userBankcardId 按卡筛选；首页可用较小 pageSize，点全部继续翻页；需登录")
	ResponseBase transactionList(PageQueryHelperEntity page,
			@RequestParam(value = "userBankcardId", required = false) Long userBankcardId,
			HttpServletRequest request);

	@PostMapping("/transfer")
	@ApiOperation(value = "钱包内部转账", notes = "需登录；recipientEmail 为收款人邮箱；sendMoney 为转出金额；payPassword 必填")
	ResponseBase transfer(@RequestBody WalletTransfetListEntity query, HttpServletRequest request);

	@GetMapping("/transferReading")
	@ApiOperation(value = "转账试算", notes = "返回手续费与实际到账；可转余额=available-open_freeze；需登录")
	ResponseBase transferReading(Double sendMoney, HttpServletRequest request);

	@GetMapping("/findReading")
	@ApiOperation(value = "查询转账费率", notes = "返回当前费率配置；需登录")
	ResponseBase findReading(HttpServletRequest request);

	@PostMapping("/walletLog")
	@ApiOperation(value = "钱包账变记录", notes = "分页；默认当月；含当月收支汇总；需登录")
	ResponseBase walletLog(@RequestBody(required = false) WalletLogEntity query, HttpServletRequest request);

	@PostMapping("/mailing/region")
	@ApiOperation(value = "查询邮寄地区列表", notes = "对齐 onetoken /accountMailing/findDelivery；透传 worldPay POST /api/delivery/region；需登录")
	ResponseBase mailingRegion(@RequestBody(required = false) WalletMailingRegionRequest query,
			HttpServletRequest request);

	@PostMapping("/mailing/add")
	@ApiOperation(value = "添加邮寄地址", notes = "对齐 onetoken /accountMailing/add；返回 id 可作为 deliveryAddressId；需登录且已开通钱包")
	ResponseBase mailingAdd(@RequestBody WalletMailingAddressAddRequest query, HttpServletRequest request);

	@PostMapping("/mailing/update")
	@ApiOperation(value = "更新邮寄地址", notes = "对齐 onetoken /accountMailing/update；id 为三方邮寄地址 id；需登录且已开通钱包")
	ResponseBase mailingUpdate(@RequestBody WalletMailingAddressUpdateRequest query, HttpServletRequest request);

	@PostMapping("/mailing/find")
	@ApiOperation(value = "查询邮寄地址", notes = "对齐 onetoken /accountMailing/find；本地分页；需登录且已开通钱包")
	ResponseBase mailingFind(@RequestBody(required = false) WalletMailingAddressFindRequest query,
			HttpServletRequest request);
}
