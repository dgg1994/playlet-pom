package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.wallet.WalletTransfetContactsEntity;
import com.playlet.oversea.entity.wallet.WalletTransfetListEntity;
import com.playlet.oversea.entity.wallet.WalletTransfetRatesEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端：内部转账费率（对齐 onetoken /walletTransfetRates）。
 * 网关：/china/admin/walletTransfetRates/**
 */
@RequestMapping("/walletTransfetRates")
@Api(value = "钱包内部转账费率", tags = "钱包内部转账费率")
public interface WalletTransferRatesManageService {

	@GetMapping("/findList")
	@ApiOperation(value = "查询费率列表", notes = "分页查询")
	ResponseBase findList();

	@PostMapping("/add")
	@ApiOperation(value = "新增费率", notes = "仅允许一条有效配置")
	ResponseBase add(WalletTransfetRatesEntity entity);

	@PostMapping("/update")
	@ApiOperation(value = "编辑费率", notes = "按 id 更新")
	ResponseBase update(WalletTransfetRatesEntity entity);

	@GetMapping("/findReading")
	@ApiOperation(value = "查询费率", notes = "返回当前费率配置")
	ResponseBase findReading();

	@GetMapping("/transferReading")
	@ApiOperation(value = "转账试算", notes = "管理端按 wallet_uid 试算手续费")
	ResponseBase transferReading(Double sendMoney, Long uid);
}
