package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.account.AppAccountEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端 APP 用户（对齐 onetoken /appUser/**）。
 */
@RequestMapping("/appUser")
@Api(value = "APP用户管理", tags = "APP用户管理")
public interface WalletAppUserManageService {

	@PostMapping("/findList")
	@ApiOperation(value = "APP用户分页列表")
	ResponseBase findList(AppAccountEntity entity);

	@GetMapping("/findKycFile")
	@ApiOperation(value = "KYC证件文件")
	ResponseBase findKycFile(String uid);
}
