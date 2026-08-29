package com.playlet.internal.service;

import com.playlet.internal.api.request.WalletCardProductUpdateRequest;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.wallet.WalletCardProductEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端 U 卡产品维护：网关 /china/admin/walletCardProductManage/**
 */
@RequestMapping("/walletCardProductManage")
@Api(value = "U卡产品管理", tags = "U卡产品管理")
public interface WalletCardProductManageService {

	@PostMapping("/findList")
	@ApiOperation(value = "卡产品分页列表", notes = "查询本地 wallet_card_product；返回含 labelList、synopsisData")
	ResponseBase findList(WalletCardProductEntity entity);

	@PostMapping("/syncFromThird")
	@ApiOperation(value = "一键同步三方卡产品", notes = "拉取三方商户卡列表写入 wallet_card_product；保留本地 card_img/enable/hot/描述")
	ResponseBase syncFromThird();

	@PostMapping("/update")
	@ApiOperation(value = "维护卡产品", notes = "按 id 更新展示图、上下架、热门、卡标签 labelList、卡简介 synopsisData")
	ResponseBase update(WalletCardProductUpdateRequest entity);
}
