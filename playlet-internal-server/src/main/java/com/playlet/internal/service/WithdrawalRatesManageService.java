package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.wallet.WalletWithdrawalRatesEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端提现费率（对齐 onetoken /withdrawalRates/**）。
 */
@RequestMapping("/withdrawalRates")
@Api(value = "提现费率", tags = "提现费率")
public interface WithdrawalRatesManageService {

	@GetMapping("/find")
	@ApiOperation(value = "查询费率")
	ResponseBase find();

	@PostMapping("/update")
	@ApiOperation(value = "更新费率")
	ResponseBase update(WalletWithdrawalRatesEntity entity);
}
