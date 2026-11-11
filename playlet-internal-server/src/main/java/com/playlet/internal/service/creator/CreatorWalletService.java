package com.playlet.internal.service.creator;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.creator.CreatorWalletApplyQuery;
import com.playlet.internal.query.creator.CreatorWalletQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 我的钱包：资金概览 / 收支明细 / 提现记录 / 提现申请。
 */
@RequestMapping("/api/creator/wallet")
@Api(value = "作家端-我的钱包", tags = "作家端-我的钱包")
public interface CreatorWalletService {

	@GetMapping("/overview")
	@ApiOperation("资金概览（余额/可提现/待结算/累计收益）")
	ResponseBase overview(HttpServletRequest request);

	@PostMapping("/ledger/list")
	@ApiOperation("收支明细（分页）")
	ResponseBase ledgerList(@RequestBody CreatorWalletQuery query, HttpServletRequest request);

	@PostMapping("/withdraw/list")
	@ApiOperation("提现记录（分页）")
	ResponseBase withdrawList(@RequestBody CreatorWalletQuery query, HttpServletRequest request);

	@PostMapping("/withdraw/apply")
	@ApiOperation("申请提现")
	ResponseBase applyWithdraw(@RequestBody CreatorWalletApplyQuery query, HttpServletRequest request);

	@GetMapping("/ledger/categories")
	@ApiOperation("收支分类字典")
	ResponseBase ledgerCategories();
}
