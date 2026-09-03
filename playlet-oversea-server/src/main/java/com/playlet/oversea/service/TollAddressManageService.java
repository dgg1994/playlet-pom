package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.wallet.WalletTollAddressEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端收款地址（对齐 onetoken /tollAddress/**）。
 */
@RequestMapping("/tollAddress")
@Api(value = "收款地址", tags = "收款地址")
public interface TollAddressManageService {

	@PostMapping("/findList")
	@ApiOperation(value = "收款地址分页")
	ResponseBase findList(WalletTollAddressEntity entity);

	@PostMapping("/add")
	@ApiOperation(value = "新增地址")
	ResponseBase add(WalletTollAddressEntity entity);

	@PostMapping("/update")
	@ApiOperation(value = "编辑地址")
	ResponseBase update(WalletTollAddressEntity entity);

	@GetMapping("/delete")
	@ApiOperation(value = "删除地址")
	ResponseBase delete(Integer id);
}
