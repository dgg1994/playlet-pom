package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.wallet.WalletCardCloseEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端销卡申请（对齐 onetoken /cardClose/**）。
 */
@RequestMapping("/cardClose")
@Api(value = "销卡申请", tags = "销卡申请")
public interface CardCloseManageService {

	@PostMapping("/findList")
	@ApiOperation(value = "销卡记录分页")
	ResponseBase findList(WalletCardCloseEntity entity);
}
