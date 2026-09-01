package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.wallet.WalletTransfetListEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端：内部转账记录（对齐 onetoken /walletTransfetList）。
 * 网关：/china/admin/walletTransfetList/**
 */
@RequestMapping("/walletTransfetList")
@Api(value = "钱包内部转账记录", tags = "钱包内部转账记录")
public interface WalletTransferListManageService {

	@PostMapping("/findList")
	@ApiOperation(value = "转账记录列表", notes = "分页查询")
	ResponseBase findList(WalletTransfetListEntity entity);
}
