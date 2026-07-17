package com.playlet.internal.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.playlet.internal.base.ResponseBase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/dic")
@Tag(name = "字典",description = "字典")
public interface DicService {

	@GetMapping("/getLanguage")
	@Operation(summary = "查询语言列表", description = "查询语言列表")
	ResponseBase getLanguage();
	
	@GetMapping("/findUserState")
	@Operation(summary = "查询用户状态", description = "查询用户状态")
	ResponseBase findUserState();
	
	@GetMapping("/findDeviceType")
	@Operation(summary = "查询设备类型", description = "查询设备类型")
	ResponseBase findDeviceType();
	
	@GetMapping("/findOrderState")
	@Operation(summary = "查询订单状态", description = "查询订单状态")
	ResponseBase findOrderState();
	
	@GetMapping("/findVerifyStatus")
	@Operation(summary = "查询审核状态类型", description = "查询审核状态类型")
	ResponseBase findVerifyStatus();

	@GetMapping("/findWelfareActionType")
	@Operation(summary = "查询每日任务类型", description = "查询每日任务类型")
	ResponseBase findWelfareActionType();
}

