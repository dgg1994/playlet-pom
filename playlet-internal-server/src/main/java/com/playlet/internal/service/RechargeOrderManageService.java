package com.playlet.internal.service;

import com.playlet.internal.api.request.DramaEpisodePayQuery;
import com.playlet.internal.api.request.DramaPaymentDetailQuery;
import com.playlet.internal.api.request.DramaPaymentPurchaseQuery;
import com.playlet.internal.api.request.DramaPaymentQuery;
import com.playlet.internal.api.request.RechargeOrderQuery;
import com.playlet.internal.api.request.RechargeOrderRefundQuery;
import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端充值订单：网关 /china/admin/rechargeOrderManage/**
 */
@RequestMapping("/rechargeOrderManage")
@Api(value = "充值订单管理", tags = "充值订单管理")
public interface RechargeOrderManageService {

	@PostMapping("/summary")
	@ApiOperation("顶部统计：今日交易额/订单数/退款率/待结算")
	ResponseBase summary();

	@PostMapping("/findList")
	@ApiOperation("订单分页列表（会员 + 单集解锁）")
	ResponseBase findList(RechargeOrderQuery query);

	@PostMapping("/detail")
	@ApiOperation("订单详情")
	ResponseBase detail(RechargeOrderRefundQuery query);

	@PostMapping("/refund")
	@ApiOperation("标记退款（仅已支付现金单；暂不调渠道）")
	ResponseBase refund(RechargeOrderRefundQuery query);

	@PostMapping("/dramaInfo")
	@ApiOperation("短剧付费数据查询")
	ResponseBase dramaInfo(DramaPaymentQuery query);

	@PostMapping("/dramaDetail")
	@ApiOperation("短剧付费详情（KPI + 剧信息 + 剧集付费摘要，默认前5集）")
	ResponseBase dramaDetail(DramaPaymentDetailQuery query);

	@PostMapping("/dramaEpisodePayList")
	@ApiOperation("剧集付费明细分页（查看全部）")
	ResponseBase dramaEpisodePayList(DramaEpisodePayQuery query);

	@PostMapping("/dramaPurchaseList")
	@ApiOperation("短剧购买记录分页查询")
	ResponseBase dramaPurchaseList(DramaPaymentPurchaseQuery query);

	@PostMapping("/dramaPaidEpisodeOptions")
	@ApiOperation("短剧付费集下拉（购买筛选）")
	ResponseBase dramaPaidEpisodeOptions(DramaPaymentDetailQuery query);
}
