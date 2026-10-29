package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.pay.PayGlobalConfigEntity;
import com.playlet.internal.entity.pay.PaySdkConfigEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 管理端支付配置：网关 /china/admin/payConfig/**
 */
@RequestMapping("/payConfig")
@Api(value = "支付配置", tags = "支付配置")
public interface PayConfigManageService {

	@GetMapping("/get")
	@ApiOperation("查询支付配置（全局配置）")
	ResponseBase get();

	@PostMapping("/global/update")
	@ApiOperation("更新支付全局配置（结算周期/提现最低金额）")
	ResponseBase updateGlobal(PayGlobalConfigEntity entity);

	@GetMapping("/sdk/list")
	@ApiOperation("查询支付 SDK 商户参数（application.yml pay.* 的 DB 覆盖配置；敏感项不回显仅返回是否已配置）")
	ResponseBase sdkList();

	@PostMapping("/sdk/update")
	@ApiOperation("更新支付 SDK 商户参数；items 中 configValue=null 表示不修改，空串表示清空（回落 yml 默认），敏感项自动加密存储")
	ResponseBase sdkUpdate(@RequestBody List<PaySdkConfigEntity> items);

	@PostMapping("/sdk/refresh")
	@ApiOperation("从 DB 重新加载支付配置并重建支付客户端（直接改库后手动刷新，无需重启）")
	ResponseBase sdkRefresh();
}
