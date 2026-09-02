package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.wallet.WalletBankcardAdminQuery;
import com.playlet.internal.query.wallet.WalletCardTransactionAdminQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端用户持卡（对齐 onetoken /appUserCard/**）。
 */
@RequestMapping("/appUserCard")
@Api(value = "用户持卡管理", tags = "用户持卡管理")
public interface AppUserCardManageService {

	@PostMapping("/pcFindUserCardList")
	@ApiOperation(value = "用户持卡分页列表")
	ResponseBase pcFindUserCardList(WalletBankcardAdminQuery query);

	@PostMapping("/findUserCardList")
	@ApiOperation(value = "指定用户持卡列表")
	ResponseBase findUserCardList(WalletBankcardAdminQuery query);

	@PostMapping("/pcFindTransaction")
	@ApiOperation(value = "卡交易流水")
	ResponseBase pcFindTransaction(WalletCardTransactionAdminQuery query);

	@GetMapping("/unfreeze")
	@ApiOperation(value = "解冻银行卡")
	ResponseBase unfreeze(Long id);
}
