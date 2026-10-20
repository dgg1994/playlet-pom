package com.playlet.internal.service;

import com.playlet.internal.api.request.CoinLedgerQueryEntity;
import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * C 端金币：网关 /entrance/api/coin/**
 */
@RequestMapping("/api/coin")
@Api(value = "金币", tags = "金币")
public interface CoinApiService {

	@GetMapping("/balance")
	@ApiOperation(value = "余额与汇率", notes = "需登录；返回余额/冻结/可用及当前 coinPerYuan")
	ResponseBase balance(HttpServletRequest request);

	@PostMapping("/ledger")
	@ApiOperation(value = "金币流水", notes = "需登录；可按 bizType 筛选，分页")
	ResponseBase ledger(HttpServletRequest request, CoinLedgerQueryEntity query);

	@GetMapping("/rate")
	@ApiOperation(value = "当前汇率", notes = "可不登录；返回多少币≈1元")
	ResponseBase rate();
}
