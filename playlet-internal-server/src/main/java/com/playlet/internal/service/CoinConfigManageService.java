package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.coin.CoinGlobalConfigEntity;
import com.playlet.internal.entity.coin.CoinRateDailyEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端金币配置：网关 /china/admin/coinConfig/**
 */
@RequestMapping("/coinConfig")
@Api(value = "金币配置", tags = "金币配置")
public interface CoinConfigManageService {

	@GetMapping("/get")
	@ApiOperation("查询全局配置")
	ResponseBase get();

	@PostMapping("/update")
	@ApiOperation("更新全局配置（汇率模式/手动汇率/清币）")
	ResponseBase update(CoinGlobalConfigEntity entity);

	@GetMapping("/rate/latest")
	@ApiOperation("最近一日汇率")
	ResponseBase latestRate();

	@PostMapping("/rate/save")
	@ApiOperation("录入/覆盖某日汇率；可传 adTotal/ecpm/rRate/coinTotal 由服务端公式计算")
	ResponseBase saveDailyRate(CoinRateDailyEntity entity);

	@PostMapping("/idleClear/run")
	@ApiOperation("手动触发一次久未登录清币（受开关控制）")
	ResponseBase runIdleClear();
}
