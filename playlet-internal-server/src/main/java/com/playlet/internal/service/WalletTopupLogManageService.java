package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.wallet.WalletUsdtTopupEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端链上充值日志（对齐 onetoken /walletTopupLog/**）。
 */
@RequestMapping("/walletTopupLog")
@Api(value = "链上充值日志", tags = "链上充值日志")
public interface WalletTopupLogManageService {

	@PostMapping("/findList")
	@ApiOperation(value = "链上充值分页")
	ResponseBase findList(WalletUsdtTopupEntity entity);
}
