package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.wallet.WalletCardSynopsisEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端卡简介（对齐 onetoken /cardSynopsis/**）。
 */
@RequestMapping("/cardSynopsis")
@Api(value = "卡简介管理", tags = "卡简介管理")
public interface CardSynopsisManageService {

	@PostMapping("/add")
	@ApiOperation(value = "新增简介")
	ResponseBase add(WalletCardSynopsisEntity entity);

	@PostMapping("/update")
	@ApiOperation(value = "编辑简介")
	ResponseBase update(WalletCardSynopsisEntity entity);

	@GetMapping("/delete")
	@ApiOperation(value = "删除简介")
	ResponseBase delete(Integer id);

	@PostMapping("/findList")
	@ApiOperation(value = "简介分页列表")
	ResponseBase findList(WalletCardSynopsisEntity entity);

	@GetMapping("/findAll")
	@ApiOperation(value = "全部简介")
	ResponseBase findAll();
}
