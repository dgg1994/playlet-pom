package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.wallet.WalletToWebLogEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端链上提现（对齐 onetoken /walletToWebLog/**）。
 */
@RequestMapping("/walletToWebLog")
@Api(value = "链上提现", tags = "链上提现")
public interface WalletToWebLogManageService {

	@PostMapping("/findList")
	@ApiOperation(value = "提现记录分页")
	ResponseBase findList(WalletToWebLogEntity entity);

	@GetMapping("/pass")
	@ApiOperation(value = "审核通过")
	ResponseBase pass(Long id, String gooleCode);

	@GetMapping("/reject")
	@ApiOperation(value = "审核拒绝")
	ResponseBase reject(Long id, String rejectContent);
}
