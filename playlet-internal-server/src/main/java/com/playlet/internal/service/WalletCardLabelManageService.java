package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.wallet.WalletCardLabelEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * U 卡标签管理（对齐 onetoken CardLableService）：网关 /china/admin/cardLable/**
 */
@RequestMapping("/cardLable")
@Api(value = "银行卡标签管理", tags = "银行卡标签管理")
public interface WalletCardLabelManageService {

	@PostMapping("/add")
	@ApiOperation(value = "新增标签", notes = "标签名称全局唯一")
	ResponseBase add(WalletCardLabelEntity entity);

	@GetMapping("/delete")
	@ApiOperation(value = "删除标签", notes = "同时删除卡产品关联")
	ResponseBase delete(Integer id);

	@PostMapping("/findList")
	@ApiOperation(value = "标签分页列表", notes = "可按 name、language 筛选")
	ResponseBase findList(WalletCardLabelEntity entity);

	@GetMapping("/findAll")
	@ApiOperation(value = "全部标签", notes = "不分页，供下拉选择")
	ResponseBase findAll();
}
