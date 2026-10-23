package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.pay.PayChannelConfigEntity;
import com.playlet.internal.entity.pay.PayGlobalConfigEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端支付配置：网关 /china/admin/payConfig/**
 */
@RequestMapping("/payConfig")
@Api(value = "支付配置", tags = "支付配置")
public interface PayConfigManageService {

	@GetMapping("/get")
	@ApiOperation("查询支付配置（渠道列表 + 全局配置）")
	ResponseBase get();

	@PostMapping("/channel/update")
	@ApiOperation("更新支付渠道配置（商户参数/启用状态）")
	ResponseBase updateChannel(PayChannelConfigEntity entity);

	@PostMapping("/global/update")
	@ApiOperation("更新支付全局配置（结算周期/提现最低金额）")
	ResponseBase updateGlobal(PayGlobalConfigEntity entity);
}
