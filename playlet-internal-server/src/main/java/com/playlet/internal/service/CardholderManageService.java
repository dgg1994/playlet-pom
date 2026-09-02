package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端持卡人（对齐 onetoken /cardholder/findByUid）。
 */
@RequestMapping("/cardholder")
@Api(value = "持卡人管理", tags = "持卡人管理")
public interface CardholderManageService {

	@GetMapping("/findByUid")
	@ApiOperation(value = "按 uid 查询持卡人")
	ResponseBase findByUid(String uid);
}
