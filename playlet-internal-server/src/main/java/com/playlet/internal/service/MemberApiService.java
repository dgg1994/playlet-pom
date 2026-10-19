package com.playlet.internal.service;

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
	@ApiOperation(value = "会员状态", notes = "需登录；返回是否会员、到期时间、免广告与会员剧权益")
	ResponseBase status(HttpServletRequest request);

	@GetMapping("/products")
	@ApiOperation(value = "套餐列表", notes = "仅返回上架套餐；可不登录")
	ResponseBase getMemberProducts();

	@PostMapping("/order/create")
	@ApiOperation(value = "创建会员订单", notes = "需登录。payType=COIN 扣金币并立即开通；"
			+ "payType=CASH 需 payChannel=WECHAT|ALIPAY|BANK，生成待支付单，成功后调 pay/notify")
	ResponseBase createOrder(HttpServletRequest request, ProductReqEntity productReqEntity);

	@PostMapping("/pay/notify")
	@ApiOperation(value = "现金支付成功确认", notes = "仅现金待支付单；幂等开通/续期。正式环境应加签验签")
	ResponseBase payNotify(MemberPayNotifyReqEntity req);
}
