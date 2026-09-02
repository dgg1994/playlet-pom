package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端 KYC 提交（对齐 onetoken /kyc/apply）。
 */
@RequestMapping("/kyc")
@Api(value = "KYC管理", tags = "KYC管理")
public interface KycManageService {

	@GetMapping("/apply")
	@ApiOperation(value = "按开卡申请提交KYC")
	ResponseBase apply(Long applyId);
}
