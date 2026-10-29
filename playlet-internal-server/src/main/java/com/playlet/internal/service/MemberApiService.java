package com.playlet.internal.service;

import com.playlet.internal.api.request.MemberContractReqEntity;
import com.playlet.internal.api.request.MemberPayNotifyReqEntity;
import com.playlet.internal.api.request.ProductReqEntity;
import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * C 端会员：网关 /entrance/api/member/**
 */
@RequestMapping("/api/member")
@Api(value = "会员", tags = "会员")
public interface MemberApiService {

	@GetMapping("/status")
	@ApiOperation(value = "会员状态", notes = "需登录；返回是否会员、到期时间、免广告与会员剧权益；"
			+ "开启连续包月时返回 autoRenew 与 nextDeductTime")
	ResponseBase status(HttpServletRequest request);

	@GetMapping("/products")
	@ApiOperation(value = "套餐列表", notes = "仅返回上架套餐；可不登录。productType=2 为连续包月套餐")
	ResponseBase getMemberProducts();

	@PostMapping("/order/create")
	@ApiOperation(value = "创建会员订单", notes = "需登录。payType=COIN 扣金币并立即开通；"
			+ "payType=CASH 需 payChannel=WECHAT|ALIPAY|BANK，生成待支付单，成功后调 pay/notify。"
			+ "可选 userCouponId：金币当场核销，现金在 pay/notify 核销。"
			+ "连续包月套餐（productType=2）不支持金币，返回 contract 供客户端拉起签约")
	ResponseBase createOrder(HttpServletRequest request, ProductReqEntity productReqEntity);

	@PostMapping("/contract/sign")
	@ApiOperation(value = "开通连续包月（签约）", notes = "需登录；同一用户重复调用会复用待签约协议。"
			+ "返回 contract：支付宝为 signForm（PC/H5 渲染自动提交表单）或 signUrl（APP 拉起串），"
			+ "微信为 signParams（端上调起签约所需）。签约成功后由三方异步通知置为已签约")
	ResponseBase signAutoRenew(HttpServletRequest request, MemberContractReqEntity req);

	@GetMapping("/contract/status")
	@ApiOperation(value = "连续包月状态", notes = "需登录；返回是否签约、每期价格、下次扣款时间")
	ResponseBase autoRenewStatus(HttpServletRequest request);

	@PostMapping("/contract/unsign")
	@ApiOperation(value = "关闭连续包月（解约）", notes = "需登录；先调三方解约，再关闭本地自动续费")
	ResponseBase unsignAutoRenew(HttpServletRequest request);

	@PostMapping("/pay/notify")
	@ApiOperation(value = "现金支付成功确认", notes = "仅现金待支付单；幂等开通/续期。正式环境应加签验签")
	ResponseBase payNotify(MemberPayNotifyReqEntity req);
}
