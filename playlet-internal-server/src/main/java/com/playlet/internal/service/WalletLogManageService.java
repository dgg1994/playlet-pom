package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.wallet.WalletLogEntity;
import com.playlet.internal.entity.wallet.WalletManualLogEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理端钱包流水（对齐 onetoken /wallet findWalletLog、walletTopUp）。
 */
@RequestMapping("/wallet")
@Api(value = "钱包管理", tags = "钱包管理")
public interface WalletLogManageService {

	@PostMapping("/findWalletLog")
	@ApiOperation(value = "钱包交易流水")
	ResponseBase findWalletLog(WalletLogEntity entity);

	@PostMapping("/walletTopUp")
	@ApiOperation(value = "人工充值/扣款")
	ResponseBase walletTopUp(WalletManualLogEntity entity, HttpServletRequest request);
}
