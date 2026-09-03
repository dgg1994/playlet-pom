package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.wallet.WalletManualLogEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端人工充值日志（对齐 onetoken /walletManualLog/**）。
 */
@RequestMapping("/walletManualLog")
@Api(value = "人工充值日志", tags = "人工充值日志")
public interface WalletManualLogManageService {

	@PostMapping("/findList")
	@ApiOperation(value = "人工充值分页")
	ResponseBase findList(WalletManualLogEntity entity);
}
